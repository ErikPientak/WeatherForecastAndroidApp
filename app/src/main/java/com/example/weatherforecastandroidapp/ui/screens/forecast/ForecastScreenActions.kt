package com.example.weatherforecastandroidapp.ui.screens.forecast

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult

sealed interface ForecastScreenActions {
    data object SearchActivated : ForecastScreenActions
    data object SearchDismissed : ForecastScreenActions
    data class SearchQueryChanged(val query: String) : ForecastScreenActions
    data class PlaceSelected(val place: PlaceSearchResult) : ForecastScreenActions
}
