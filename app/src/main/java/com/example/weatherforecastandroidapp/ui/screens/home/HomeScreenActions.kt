package com.example.weatherforecastandroidapp.ui.screens.home

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult

sealed interface HomeScreenActions {
    data object Retry : HomeScreenActions
    data object LocationPermissionDenied : HomeScreenActions
    data object LocationPermissionGranted : HomeScreenActions

    //SearchBar
    data object SearchActivated : HomeScreenActions
    data object SearchDismissed : HomeScreenActions
    data class SearchQueryChanged(val query: String) : HomeScreenActions
    data class PlaceSelected(val place: PlaceSearchResult) : HomeScreenActions
}
