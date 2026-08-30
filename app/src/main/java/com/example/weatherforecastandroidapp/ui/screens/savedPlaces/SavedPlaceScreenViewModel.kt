package com.example.weatherforecastandroidapp.ui.screens.savedPlaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastandroidapp.data.model.SavedPlace
import com.example.weatherforecastandroidapp.data.model.WeatherForecast
import com.example.weatherforecastandroidapp.data.repository.PlacesRepository
import com.example.weatherforecastandroidapp.data.repository.WeatherRepository
import com.example.weatherforecastandroidapp.ui.elements.cards.SavedLocationWeather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SavedPlaceScreenViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SavedPlacesScreenUiState>(SavedPlacesScreenUiState.Loading)
    val uiState: StateFlow<SavedPlacesScreenUiState> = _uiState.asStateFlow()

    init {
        loadSavedPlacesForecast()
    }

    private fun loadSavedPlacesForecast() {
        viewModelScope.launch {
            _uiState.value = SavedPlacesScreenUiState.Loading

            placesRepository.observeSavedPlaces().collect { places ->
                val savedLocationsWeather = places.mapNotNull { place ->
                    weatherRepository.getForecast(place.latitude, place.longitude)
                        .getOrNull()
                        ?.let { forecast -> place.toSavedLocationWeather(forecast) }
                }
                _uiState.value = SavedPlacesScreenUiState.Success(savedLocationsWeather)
            }
        }
    }

    // WeatherForecast.current.time is a local ISO string with no offset (repo requests
    // timezone = "auto"), so it's already this place's own local time and LocalDateTime.parse
    // works directly. isDay comes straight from the API's own is_day flag for that place, not a
    // device-local heuristic, since saved places can be in different day/night states at once.
    private fun SavedPlace.toSavedLocationWeather(forecast: WeatherForecast) = SavedLocationWeather(
        cityName = name,
        localTime = LocalDateTime.parse(forecast.current.time).format(TIME_FORMATTER),
        temperature = forecast.current.temperature.toInt(),
        weatherCode = forecast.current.weatherCode,
        isDay = forecast.current.isDay,
        highTemperature = forecast.daily[0].tempMax.toInt(),
        lowTemperature = forecast.daily[0].tempMin.toInt(),
    )

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
