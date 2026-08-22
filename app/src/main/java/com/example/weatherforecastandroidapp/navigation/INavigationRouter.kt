package com.example.weatherforecastandroidapp.navigation

interface INavigationRouter {
    fun returnBack()
    fun navigateTo(destination: Destination)
}