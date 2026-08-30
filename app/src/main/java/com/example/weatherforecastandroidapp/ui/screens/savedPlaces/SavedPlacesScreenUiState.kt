package com.example.weatherforecastandroidapp.ui.screens.savedPlaces

import androidx.annotation.StringRes
import com.example.weatherforecastandroidapp.ui.elements.cards.SavedLocationWeather

sealed interface SavedPlacesScreenUiState {
    data object Loading : SavedPlacesScreenUiState
    data class Error(@param:StringRes val errorMessage: Int) : SavedPlacesScreenUiState
    data class Success(
        val places: List<SavedLocationWeather>,
    ) : SavedPlacesScreenUiState
}
