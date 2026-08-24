package com.example.weatherforecastandroidapp.data.model

data class WeatherForecast(
    val current: CurrentConditions,
    val hourly: List<HourlyEntry>,
    val daily: List<DailyEntry>,
)

data class CurrentConditions(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val pressure: Double,
    val isDay: Boolean,
    val weatherCode: Int,
    val dewPoint: Double,
)

data class HourlyEntry(
    val time: String,
    val temperature: Double,
    val precipitationProbability: Int,
    val weatherCode: Int,
    val uvIndex: Double,
)

data class DailyEntry(
    val date: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationProbabilityMax: Int,
    val sunrise: String,
    val sunset: String,
    val uvIndexMax: Double,
    val daylightDuration: Double,
)
