package com.example.weatherforecastandroidapp.ui.screens.home

import androidx.annotation.StringRes
import com.example.weatherforecastandroidapp.ui.elements.cards.HourlyForecastItem

sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState
    data class Error(@param:StringRes val errorMessage: Int) : HomeScreenUiState
    data class Success(
        val temperature: Int,
        val weatherCode: Int,
        val isDay: Boolean,
        val highTemperature: Int,
        val lowTemperature: Int,
        val hourlyForecast: List<HourlyForecastItem>,
    ) : HomeScreenUiState
    data object PermissionRequired : HomeScreenUiState
}