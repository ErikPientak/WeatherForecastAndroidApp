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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
            viewModelScope.launch { placesRepository.addPlace(location.place) }
        }
    }

    private fun loadForecast() {
        viewModelScope.launch {
            _uiState.value = ForecastUiState.Loading

            val latitude: Double
            val longitude: Double
            val locationName: String

            when (val location = activeLocationController.activeLocation.value) {
                ActiveLocation.Gps -> {
                    val gpsLocation = locationTracker.getCurrentLocation()
                    if (gpsLocation == null) {
                        _uiState.value = ForecastUiState.Error(R.string.error_location_not_found)
                        return@launch
                    }
                    latitude = gpsLocation.latitude
                    longitude = gpsLocation.longitude
                    locationName = ""
                }
                is ActiveLocation.Searched -> {
                    latitude = location.place.latitude
                    longitude = location.place.longitude
                    locationName = location.place.name
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
