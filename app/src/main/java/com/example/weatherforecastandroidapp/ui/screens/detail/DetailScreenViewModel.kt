package com.example.weatherforecastandroidapp.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastandroidapp.data.model.HourlyEntry
import com.example.weatherforecastandroidapp.data.model.SavedPlace
import com.example.weatherforecastandroidapp.data.repository.PlacesRepository
import com.example.weatherforecastandroidapp.data.repository.WeatherRepository
import com.example.weatherforecastandroidapp.ui.elements.cards.HourlyForecastItem
import com.example.weatherforecastandroidapp.ui.elements.cards.PrecipitationPoint
import com.example.weatherforecastandroidapp.ui.elements.cards.WeeklyForecastDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val placesRepository: PlacesRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow<DetailScreenUiState>(DetailScreenUiState.Loading)
    val uiState: StateFlow<DetailScreenUiState> = _uiState.asStateFlow()

    fun loadForecast(latitude: Double, longitude: Double, placeName: String) {
        viewModelScope.launch {
            _uiState.value = DetailScreenUiState.Loading

            weatherRepository.getForecast(latitude, longitude).onSuccess { forecast ->
                _uiState.value = DetailScreenUiState.Success(
                    locationName = placeName,
                    temperature = forecast.current.temperature.toInt(),
                    weatherCode = forecast.current.weatherCode,
                    // Unlike HomeScreenViewModel (device's own location, no isDay from the API
                    // response usable in the same way), Detail shows an arbitrary place, so the
                    // API's own current.isDay is the correct source — same convention as
                    // SavedPlaceScreenViewModel, since a saved/searched place can be in a
                    // different day/night state than the device's local time.
                    isDay = forecast.current.isDay,
                    highTemperature = forecast.daily[0].tempMax.toInt(),
                    lowTemperature = forecast.daily[0].tempMin.toInt(),
                    humidity = forecast.current.humidity,
                    windSpeed = forecast.current.windSpeed,
                    windDirection = forecast.current.windDirection,
                    pressure = forecast.current.pressure,
                    dewPoint = forecast.current.dewPoint.toInt(),
                    hourlyForecast = forecast.hourly.toForecastItems(),
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
                    dailyPrecipitation = forecast.daily.map {
                        PrecipitationPoint(
                            label = LocalDate.parse(it.date)
                                .dayOfWeek
                                .getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            percentage = it.precipitationProbabilityMax
                        )
                    },
                    hourlyPrecipitation = forecast.hourly.toPrecipitationPoints()
                )
            }
        }
    }

    fun deletePlace(place: SavedPlace, onDeleted: () -> Unit) {
        viewModelScope.launch {
            placesRepository.removePlace(place)
            onDeleted()
        }
    }

    // Mirrors HomeScreenViewModel.toForecastItems(): HourlyEntry.time is a local ISO string with
    // no offset (repo requests timezone = "auto"), so LocalDateTime.parse works directly.
    private fun List<HourlyEntry>.toForecastItems(): List<HourlyForecastItem> {
        val today = LocalDate.now()
        val now = LocalDateTime.now()

        return this
            .filter { LocalDateTime.parse(it.time).toLocalDate() == today }
            .take(HOURLY_ITEM_COUNT)
            .map { entry ->
                val label = if (LocalDateTime.parse(entry.time).hour == now.hour) "Now"
                else LocalDateTime.parse(entry.time).format(TIME_FORMATTER)
                HourlyForecastItem(
                    label = label,
                    temperature = entry.temperature.toInt(),
                    weatherCode = entry.weatherCode,
                    uvIndex = entry.uvIndex,
                    isDay = LocalDateTime.now().hour in 6..18,
                )
            }
    }

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
        const val HOURLY_ITEM_COUNT = 24
        const val HOUR_STEP = 4
        const val MAX_HOURLY_POINTS = 5
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
