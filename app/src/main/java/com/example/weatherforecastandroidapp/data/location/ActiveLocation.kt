package com.example.weatherforecastandroidapp.data.location

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult

sealed interface ActiveLocation {
    data object Gps : ActiveLocation
    data class Searched(val place: PlaceSearchResult) : ActiveLocation
}
