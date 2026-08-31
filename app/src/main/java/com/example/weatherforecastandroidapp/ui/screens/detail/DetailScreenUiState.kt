package com.example.weatherforecastandroidapp.ui.screens.detail

import androidx.annotation.StringRes
import com.example.weatherforecastandroidapp.ui.elements.cards.HourlyForecastItem
import com.example.weatherforecastandroidapp.ui.elements.cards.PrecipitationPoint
import com.example.weatherforecastandroidapp.ui.elements.cards.WeeklyForecastDay

sealed interface DetailScreenUiState {
    data object Loading : DetailScreenUiState
    data class Error(@param:StringRes val errorMessage: Int) : DetailScreenUiState
    data class Success(
        val locationName: String,
        val temperature: Int,
        val weatherCode: Int,
        val isDay: Boolean,
        val highTemperature: Int,
        val lowTemperature: Int,
        val humidity: Int,
        val windSpeed: Double,
        val windDirection: Int,
        val pressure: Double,
        val dewPoint: Int,
        val hourlyForecast: List<HourlyForecastItem>,
        val dailyPrecipitation: List<PrecipitationPoint>,
        val hourlyPrecipitation: List<PrecipitationPoint>,
        val weeklyForecast: List<WeeklyForecastDay>,
    ) : DetailScreenUiState
}
