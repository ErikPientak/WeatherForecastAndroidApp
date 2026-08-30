package com.example.weatherforecastandroidapp.ui.screens.savedPlaces

import com.example.weatherforecastandroidapp.data.model.SavedPlace

interface SavedPlacesScreenActions {
    fun deletePlace(place: SavedPlace)
}