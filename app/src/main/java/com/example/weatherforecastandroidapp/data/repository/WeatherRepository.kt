package com.example.weatherforecastandroidapp.data.repository

import com.example.weatherforecastandroidapp.data.model.WeatherForecast

interface WeatherRepository {
    suspend fun getForecast(latitude: Double, longitude: Double): Result<WeatherForecast>
}
