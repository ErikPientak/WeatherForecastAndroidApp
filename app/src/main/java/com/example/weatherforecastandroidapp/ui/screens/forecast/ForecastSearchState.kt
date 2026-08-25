package com.example.weatherforecastandroidapp.ui.screens.forecast

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult

data class ForecastSearchState(
    val isActive: Boolean = false,
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<PlaceSearchResult> = emptyList(),
)
