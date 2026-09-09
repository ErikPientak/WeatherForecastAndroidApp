package com.example.weatherforecastandroidapp.ui.screens.savedPlaces

import com.example.weatherforecastandroidapp.data.model.SavedPlace

sealed interface SavedPlacesScreenActions {
    data class removePlace(val place: SavedPlace) : SavedPlacesScreenActions
}