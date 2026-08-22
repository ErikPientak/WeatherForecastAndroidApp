package com.example.weatherforecastandroidapp.data.model

data class SavedPlace(
    val id: Long,
    val name: String,
    val admin1: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
)
