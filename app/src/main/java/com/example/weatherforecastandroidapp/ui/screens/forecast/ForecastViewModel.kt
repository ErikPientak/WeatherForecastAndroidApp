package com.example.weatherforecastandroidapp.ui.screens.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.data.location.ActiveLocation
import com.example.weatherforecastandroidapp.data.location.ActiveLocationController
import com.example.weatherforecastandroidapp.data.location.LocationTracker
import com.example.weatherforecastandroidapp.data.model.HourlyEntry
import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.repository.PlacesRepository
import com.example.weatherforecastandroidapp.data.repository.WeatherRepository
import com.example.weatherforecastandroidapp.ui.elements.cards.PrecipitationPoint
import com.example.weatherforecastandroidapp.ui.elements.cards.WeeklyForecastDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val placesRepository: PlacesRepository,
    private val activeLocationController: ActiveLocationController,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    // isActive (is the search bar showing) is the only piece of ForecastSearchState this screen
    // still owns locally — query/results/isSearching are mirrored in from ActiveLocationController
    // below, since that's now the single shared source of truth for search across screens.
    private val _searchState = MutableStateFlow(ForecastSearchState())
    val searchState: StateFlow<ForecastSearchState> = _searchState.asStateFlow()

    // Gates the Gps branch of loadForecast(). activeLocation starts at Gps by default and the
    // collector below fires on that value immediately at ViewModel creation — before ForecastScreen's
    // LaunchedEffect has had a chance to check/request ACCESS_FINE_LOCATION. Without this flag,
    // loadForecast() would call LocationTracker.getCurrentLocation() (which assumes permission was
    // already confirmed) before it actually was, risking a SecurityException on first launch.
    private var hasLocationPermission = false

    // One-off "place saved"/"already saved" events for the UI to surface as a Snackbar. A
    // SharedFlow (not StateFlow) so a repeat save re-emits the same message instead of being
    // deduped, and so a message isn't replayed to a new collector after being shown once.
    private val _saveResultEvent = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val saveResultEvent: SharedFlow<Int> = _saveResultEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            activeLocationController.activeLocation.collect { loadForecast() }
        }
        viewModelScope.launch {
            activeLocationController.searchQuery.collect { query ->
                _searchState.update { it.copy(query = query) }
            }
        }
        viewModelScope.launch {
            activeLocationController.searchResults.collect { results ->
                _searchState.update { it.copy(results = results) }
            }
        }
        viewModelScope.launch {
            activeLocationController.isSearching.collect { isSearching ->
                _searchState.update { it.copy(isSearching = isSearching) }
            }
        }
    }

    fun onAction(action: ForecastScreenActions) {
        when (action) {
            ForecastScreenActions.LocationPermissionGranted -> {
                hasLocationPermission = true
                loadForecast()
            }
            ForecastScreenActions.LocationPermissionDenied -> {
                hasLocationPermission = false
                _uiState.value = ForecastUiState.PermissionRequired
            }
            ForecastScreenActions.SearchActivated -> _searchState.update { it.copy(isActive = true) }
            ForecastScreenActions.SearchDismissed -> {
                activeLocationController.onSearchQueryChange("")
                _searchState.update { it.copy(isActive = false) }
            }
            is ForecastScreenActions.SearchQueryChanged -> activeLocationController.onSearchQueryChange(action.query)
            is ForecastScreenActions.PlaceSelected -> onPlaceSelected(action.place)
            ForecastScreenActions.PlaceSaved -> onPlaceSaved()
        }
    }

    private fun onPlaceSelected(place: PlaceSearchResult) {
        _searchState.update { it.copy(isActive = false) }
        activeLocationController.selectPlace(place)
    }

    // Only a Searched location has anything meaningful to save; GPS has no PlaceSearchResult to
    // pass to PlacesRepository.addPlace, so saving while on GPS is a no-op.
    private fun onPlaceSaved() {
        val location = activeLocationController.activeLocation.value
        if (location is ActiveLocation.Searched) {
            viewModelScope.launch {
                val newlySaved = placesRepository.addPlace(location.place)
                _saveResultEvent.emit(
                    if (newlySaved) R.string.place_saved_message else R.string.place_already_saved_message
                )
                _uiState.update { state ->
                    if (state is ForecastUiState.Success) state.copy(isSaved = true) else state
                }
            }
        }
    }

    private fun loadForecast() {
        viewModelScope.launch {
            val location = activeLocationController.activeLocation.value

            // Permission not confirmed yet: bail out without touching LocationTracker or uiState.
            // ForecastScreen's LaunchedEffect will dispatch LocationPermissionGranted/Denied once the
            // permission check/request resolves, which calls back into this function again.
            if (location is ActiveLocation.Gps && !hasLocationPermission) {
                return@launch
            }

            _uiState.value = ForecastUiState.Loading

            val latitude: Double
            val longitude: Double
            val locationName: String
            val isSaved: Boolean

            when (location) {
                ActiveLocation.Gps -> {
                    val gpsLocation = locationTracker.getCurrentLocation()
                    if (gpsLocation == null) {
                        _uiState.value = ForecastUiState.Error(R.string.error_location_not_found)
                        return@launch
                    }
                    latitude = gpsLocation.latitude
                    longitude = gpsLocation.longitude
                    locationName = ""
                    isSaved = false
                }
                is ActiveLocation.Searched -> {
                    latitude = location.place.latitude
                    longitude = location.place.longitude
                    locationName = location.place.name
                    isSaved = placesRepository.isPlaceSaved(latitude, longitude)
                }
            }

            weatherRepository.getForecast(latitude, longitude)
                .onSuccess { forecast ->
                    _uiState.value = ForecastUiState.Success(
                        weeklyForecast = forecast.daily.map {
                            WeeklyForecastDay(
                                dayLabel = LocalDate.parse(it.date)
                                    .dayOfWeek
                                    .getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                weatherCode = it.weatherCode,
                                precipitationChance = it.precipitationProbabilityMax,
                                lowTemperature = it.tempMin.toInt(),
                                highTemperature = it.tempMax.toInt()
                            )
                        },
                        dailyForecast = forecast.daily.map {
                            PrecipitationPoint(
                                label = LocalDate.parse(it.date)
                                    .dayOfWeek
                                    .getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                percentage = it.precipitationProbabilityMax
                            )
                        },
                        hourlyForecast = forecast.hourly.toPrecipitationPoints(),
                        locationName = locationName,
                        isSaved = isSaved,
                    )
                }
                .onFailure { _uiState.value = ForecastUiState.Error(R.string.error_could_not_load_forecast) }
        }
    }

    // HourlyEntry.time is a local ISO string with no offset (repo requests timezone = "auto"),
    // so LocalDateTime.parse works directly without any timezone conversion.
    private fun List<HourlyEntry>.toPrecipitationPoints(): List<PrecipitationPoint> {
        val currentHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        val startIndex = indexOfFirst { LocalDateTime.parse(it.time) >= currentHour }.coerceAtLeast(0)

        return (startIndex until size step HOUR_STEP)
            .take(MAX_HOURLY_POINTS)
            .map { index ->
                val entry = this[index]
                val label = if (index == startIndex) "Now"
                else LocalDateTime.parse(entry.time).format(TIME_FORMATTER)
                PrecipitationPoint(label = label, percentage = entry.precipitationProbability)
            }
    }

    private companion object {
        const val HOUR_STEP = 4
        const val MAX_HOURLY_POINTS = 5
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
