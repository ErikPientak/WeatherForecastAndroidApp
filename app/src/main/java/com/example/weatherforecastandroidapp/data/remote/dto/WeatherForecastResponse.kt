package com.example.weatherforecastandroidapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeatherForecastResponse(
    val current: CurrentDto,
    val hourly: HourlyDto,
    val daily: DailyDto,
)

@Serializable
data class CurrentDto(
    val time: String,
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val apparent_temperature: Double,
    val is_day: Int,
    val precipitation: Double,
    val weather_code: Int,
    val surface_pressure: Double,
    val wind_speed_10m: Double,
    val wind_direction_10m: Int,
    val dew_point_2m: Double,
)

@Serializable
data class HourlyDto(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>,
    val precipitation_probability: List<Int>,
    val uv_index: List<Double>,
)

@Serializable
data class DailyDto(
    val time: List<String>,
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val precipitation_probability_max: List<Int>,
    val sunrise: List<String>,
    val sunset: List<String>,
    val uv_index_max: List<Double>,
    val daylight_duration: List<Double>,
)
