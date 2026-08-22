package com.example.weatherforecastandroidapp.navigation

import androidx.navigation.NavController

class NavigationRouterImpl(private val navController: NavController) : INavigationRouter {
    override fun returnBack() {
        navController.navigateUp()
    }

    override fun navigateTo(destination: Destination) {
        navController.navigate(destination)
    }
}