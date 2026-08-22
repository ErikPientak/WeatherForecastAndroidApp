package com.example.weatherforecastandroidapp.ui.screens.home

sealed interface HomeScreenActions {
    data object Retry : HomeScreenActions
    data object LocationPermissionDenied : HomeScreenActions
    data object LocationPermissionGranted : HomeScreenActions
}