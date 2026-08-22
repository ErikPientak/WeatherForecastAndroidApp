package com.example.weatherforecastandroidapp.data.repository

import com.example.weatherforecastandroidapp.data.model.CurrentConditions
import com.example.weatherforecastandroidapp.data.model.DailyEntry
import com.example.weatherforecastandroidapp.data.model.HourlyEntry
import com.example.weatherforecastandroidapp.data.model.WeatherForecast
import com.example.weatherforecastandroidapp.data.remote.WeatherApiService
import com.example.weatherforecastandroidapp.data.remote.dto.WeatherForecastResponse
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
) : WeatherRepository {

    override suspend fun getForecast(latitude: Double, longitude: Double): Result<WeatherForecast> =
        runCatching {
            weatherApiService.getForecast(
                latitude = latitude,
                longitude = longitude,
                current = CURRENT_FIELDS,
                hourly = HOURLY_FIELDS,
                daily = DAILY_FIELDS,
                timezone = "auto",
                forecastDays = 7,
            ).toDomain()
        }

    private fun WeatherForecastResponse.toDomain(): WeatherForecast = WeatherForecast(
        current = CurrentConditions(
            temperature = current.temperature_2m,
            apparentTemperature = current.apparent_temperature,
            humidity = current.relative_humidity_2m,
            windSpeed = current.wind_speed_10m,
            windDirection = current.wind_direction_10m,
            pressure = current.surface_pressure,
            isDay = current.is_day == 1,
            weatherCode = current.weather_code,
        ),
        hourly = hourly.time.indices.map { i ->
            HourlyEntry(
                time = hourly.time[i],
                temperature = hourly.temperature_2m[i],
                precipitationProbability = hourly.precipitation_probability[i],
                weatherCode = hourly.weather_code[i],
            )
        },
        daily = daily.time.indices.map { i ->
            DailyEntry(
                date = daily.time[i],
                weatherCode = daily.weather_code[i],
                tempMax = daily.temperature_2m_max[i],
                tempMin = daily.temperature_2m_min[i],
                precipitationProbabilityMax = daily.precipitation_probability_max[i],
                sunrise = daily.sunrise[i],
                sunset = daily.sunset[i],
            )
        },
    )

    private companion object {
        const val CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation," +
                "weather_code,surface_pressure,wind_speed_10m,wind_direction_10m"
        const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability"
        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max," +
                "sunrise,sunset"
    }
}
