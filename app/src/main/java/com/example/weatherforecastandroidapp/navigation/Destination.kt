package com.example.weatherforecastandroidapp.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable data object HomeScreen : Destination
    @Serializable data object ForecastScreen : Destination
    @Serializable data object SavedPlacesScreen : Destination
    @Serializable data object SearchPlaceScreen : Destination
    @Serializable data object SettingsScreen : Destination
    @Serializable data class DetailScreen(val placeId: Long, val placeName: String, val placeLatitude: Double, val placeLongitude: Double) : Destination
}