package com.example.weatherforecastandroidapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.data.location.ActiveLocation
import com.example.weatherforecastandroidapp.data.location.ActiveLocationController
import com.example.weatherforecastandroidapp.data.location.LocationTracker
import com.example.weatherforecastandroidapp.data.model.HourlyEntry
import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.repository.WeatherRepository
import com.example.weatherforecastandroidapp.ui.elements.cards.HourlyForecastItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val activeLocationController: ActiveLocationController,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Loading)
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    // isActive (is the search bar showing) is the only piece of HomeScreenSearchState this screen
    // owns locally — query/results/isSearching are mirrored in from ActiveLocationController
    // below, since that's the single shared source of truth for search across screens.
    private val _searchState = MutableStateFlow(HomeScreenSearchState())
    val searchState: StateFlow<HomeScreenSearchState> = _searchState.asStateFlow()

    // Gates the Gps branch of loadForecast(). activeLocation starts at Gps by default and the
    // collector below fires on that value immediately at ViewModel creation — before HomeScreen's
    // LaunchedEffect has had a chance to check/request ACCESS_FINE_LOCATION. Without this flag,
    // loadForecast() would call LocationTracker.getCurrentLocation() (which assumes permission was
    // already confirmed) before it actually was, risking a SecurityException on first launch.
    private var hasLocationPermission = false

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

    fun onAction(action: HomeScreenActions) {
        when (action) {
            HomeScreenActions.Retry -> loadForecast()
            HomeScreenActions.LocationPermissionGranted -> {
                hasLocationPermission = true
                loadForecast()
            }
            HomeScreenActions.LocationPermissionDenied -> {
                hasLocationPermission = false
                _uiState.value = HomeScreenUiState.PermissionRequired
            }
            HomeScreenActions.SearchActivated -> _searchState.update { it.copy(isActive = true) }
            HomeScreenActions.SearchDismissed -> {
                activeLocationController.onSearchQueryChange("")
                _searchState.update { it.copy(isActive = false) }
            }
            is HomeScreenActions.SearchQueryChanged -> activeLocationController.onSearchQueryChange(action.query)
            is HomeScreenActions.PlaceSelected -> onPlaceSelected(action.place)
        }
    }

    private fun onPlaceSelected(place: PlaceSearchResult) {
        _searchState.update { it.copy(isActive = false) }
        activeLocationController.selectPlace(place)
    }

    private fun loadForecast() {
        viewModelScope.launch {
            val location = activeLocationController.activeLocation.value

            // Permission not confirmed yet: bail out without touching LocationTracker or uiState.
            // HomeScreen's LaunchedEffect will dispatch LocationPermissionGranted/Denied once the
            // permission check/request resolves, which calls back into this function again.
            if (location is ActiveLocation.Gps && !hasLocationPermission) {
                return@launch
            }

            _uiState.value = HomeScreenUiState.Loading

            val latitude: Double
            val longitude: Double
            val locationName: String

            when (location) {
                ActiveLocation.Gps -> {
                    val gpsLocation = locationTracker.getCurrentLocation()
                    if (gpsLocation == null) {
                        _uiState.value = HomeScreenUiState.Error(R.string.error_location_not_found)
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
                    _uiState.value = HomeScreenUiState.Success(
                        temperature = forecast.current.temperature.toInt(),
                        weatherCode = forecast.current.weatherCode,
                        isDay = LocalDateTime.now().hour in 6..18,
                        highTemperature = forecast.daily[0].tempMax.toInt(),
                        lowTemperature = forecast.daily[0].tempMin.toInt(),
                        humidity = forecast.current.humidity,
                        windSpeed = forecast.current.windSpeed,
                        windDirection = forecast.current.windDirection,
                        pressure = forecast.current.pressure,
                        dewPoint = forecast.current.dewPoint.toInt(),
                        hourlyForecast = forecast.hourly.toForecastItems(),
                    )
                }
                .onFailure { _uiState.value = HomeScreenUiState.Error(R.string.error_could_not_load_forecast) }
        }
    }

    // HourlyEntry.time is a local ISO string with no offset (repo requests timezone = "auto"),
    // so LocalDateTime.parse works directly without any timezone conversion.
    private fun List<HourlyEntry>.toForecastItems(): List<HourlyForecastItem> {
        val today = LocalDate.now()
        val now = LocalDateTime.now()

        return this
            .filter { LocalDateTime.parse(it.time).toLocalDate() == today }
            .take(HOURLY_ITEM_COUNT)
            .mapIndexed { index, entry ->
                val label = if (LocalDateTime.parse(entry.time).hour == now.hour) "Now"
                else LocalDateTime.parse(entry.time).format(TIME_FORMATTER)
                HourlyForecastItem(
                    label = label,
                    temperature = entry.temperature.toInt(),
                    weatherCode = entry.weatherCode,
                    uvIndex = entry.uvIndex,
                    // HourlyEntry has no isDay of its own (only CurrentConditions does); defaulting
                    // to true until a real sunrise/sunset comparison is worth adding.
                    isDay = LocalDateTime.now().hour in 6..18,
                )
            }
    }

    private companion object {
        const val HOURLY_ITEM_COUNT = 24
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

