package com.example.weatherforecastandroidapp.ui.screens.forecast

import androidx.annotation.StringRes
import com.example.weatherforecastandroidapp.ui.elements.cards.PrecipitationPoint
import com.example.weatherforecastandroidapp.ui.elements.cards.WeeklyForecastDay

sealed interface ForecastUiState {

    data object Loading : ForecastUiState
    data class Error(@param:StringRes val errorMessage: Int) : ForecastUiState
    data class Success(
        val dailyForecast: List<PrecipitationPoint>,
        val hourlyForecast: List<PrecipitationPoint>,
        val weeklyForecast: List<WeeklyForecastDay>,
        val locationName: String
    ) : ForecastUiState

}