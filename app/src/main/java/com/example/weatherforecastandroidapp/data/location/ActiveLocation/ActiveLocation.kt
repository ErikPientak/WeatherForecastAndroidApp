package com.example.weatherforecastandroidapp.data.location.ActiveLocation

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult

sealed interface ActiveLocation {
    data object Gps : ActiveLocation
    data class SearchedLocation(val place: PlaceSearchResult): ActiveLocation
}