package com.example.weatherforecastandroidapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.data.location.LocationTracker
import com.example.weatherforecastandroidapp.data.model.HourlyEntry
import com.example.weatherforecastandroidapp.data.repository.WeatherRepository
import com.example.weatherforecastandroidapp.ui.elements.cards.HourlyForecastItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationTracker: LocationTracker,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Loading)
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    fun onAction(action: HomeScreenActions) {
        when (action) {
            HomeScreenActions.Retry,
            HomeScreenActions.LocationPermissionGranted -> loadForecast()
            HomeScreenActions.LocationPermissionDenied -> _uiState.value = HomeScreenUiState.PermissionRequired
        }
    }

    private fun loadForecast() {
        viewModelScope.launch {
            _uiState.value = HomeScreenUiState.Loading
            val location = locationTracker.getCurrentLocation()
            if (location == null) {
                _uiState.value = HomeScreenUiState.Error(R.string.error_location_not_found)
                return@launch
            }
            weatherRepository.getForecast(location.latitude, location.longitude)
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

