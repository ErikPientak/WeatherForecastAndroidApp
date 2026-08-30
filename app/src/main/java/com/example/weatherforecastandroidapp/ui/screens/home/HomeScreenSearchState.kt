package com.example.weatherforecastandroidapp.ui.screens.home

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult

data class HomeScreenSearchState (
    val isActive: Boolean = false,
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<PlaceSearchResult> = emptyList(),
)