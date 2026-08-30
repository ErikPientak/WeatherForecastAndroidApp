package com.example.weatherforecastandroidapp.ui.screens.forecast

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.data.location.LocationTracker
import com.example.weatherforecastandroidapp.data.model.HourlyEntry
import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.repository.PlacesRepository
import com.example.weatherforecastandroidapp.data.repository.WeatherRepository
import com.example.weatherforecastandroidapp.ui.elements.cards.PrecipitationPoint
import com.example.weatherforecastandroidapp.ui.elements.cards.WeeklyForecastDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import kotlin.time.Duration.Companion.milliseconds


@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val placesRepository: PlacesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    private val _searchState = MutableStateFlow(ForecastSearchState())
    val searchState: StateFlow<ForecastSearchState> = _searchState.asStateFlow()

    // Overrides GPS location once a search result is picked. Session-only (not persisted) per the
    // chosen UX: search reloads this screen's forecast, it doesn't touch Home/GPS or Saved Places.
    private var selectedLocation: PlaceSearchResult? = null
    private var searchJob: Job? = null

    init {
        loadForecast()
    }

    fun onAction(action: ForecastScreenActions) {
        when (action) {
            ForecastScreenActions.SearchActivated -> _searchState.update { it.copy(isActive = true) }
            ForecastScreenActions.SearchDismissed -> {
                searchJob?.cancel()
                _searchState.value = ForecastSearchState()
            }
            is ForecastScreenActions.SearchQueryChanged -> onSearchQueryChanged(action.query)
            is ForecastScreenActions.PlaceSelected -> onPlaceSelected(action.place)
            is ForecastScreenActions.PlaceSaved -> selectedLocation?.let { onPlaceSaved(it) }
        }
    }

    private fun onSearchQueryChanged(query: String) {
        _searchState.update { it.copy(query = query) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS.milliseconds)
            _searchState.update { it.copy(isSearching = true) }
            placesRepository.search(query)
                .onSuccess { results -> _searchState.update { it.copy(results = results, isSearching = false) } }
                .onFailure { _searchState.update { it.copy(results = emptyList(), isSearching = false) } }
        }
    }

    private fun onPlaceSelected(place: PlaceSearchResult) {
        searchJob?.cancel()
        selectedLocation = place
        _searchState.value = ForecastSearchState()
        loadForecast()
    }

    private fun onPlaceSaved(place: PlaceSearchResult){
        viewModelScope.launch {
            placesRepository.addPlace(place)
        }
        Log.d("ForecastViewModel", "Saved place: $place")
    }

    private fun loadForecast() {
        viewModelScope.launch {
            _uiState.value = ForecastUiState.Loading

            val override = selectedLocation
            val latitude: Double
            val longitude: Double
            if (override != null) {
                latitude = override.latitude
                longitude = override.longitude
            } else {
                val location = locationTracker.getCurrentLocation()
                if (location == null) {
                    _uiState.value = ForecastUiState.Error(R.string.error_location_not_found)
                    return@launch
                }
                latitude = location.latitude
                longitude = location.longitude
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
                        locationName = selectedLocation?.name ?: ""
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
        const val SEARCH_DEBOUNCE_MS = 350L
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}