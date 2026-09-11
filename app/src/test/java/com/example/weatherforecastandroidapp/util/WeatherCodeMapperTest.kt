package com.example.weatherforecastandroidapp.util

import com.example.weatherforecastandroidapp.R
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeMapperTest {
    @Test
    fun `weather code 0 is clear sky`() {
        assertEquals(R.string.weather_clear_sky, WeatherCodeMapper.descriptionRes(0))
        assertEquals("☀️", WeatherCodeMapper.emoji(0))
    }
    @Test
    fun `weather code 0 at night is moon emoji`() {
        assertEquals("🌙", WeatherCodeMapper.emoji(0, isDay = false))
    }
    @Test
    fun `weather code 1 is mainly clear`() {
        assertEquals(R.string.weather_mainly_clear, WeatherCodeMapper.descriptionRes(1))
        assertEquals("🌤️", WeatherCodeMapper.emoji(1))
    }
    @Test
    fun `weather code 1 at night is cloud emoji`() {
        assertEquals("☁️", WeatherCodeMapper.emoji(1, isDay = false))
    }
    @Test
    fun `weather code 2 is partly cloudy`() {
        assertEquals(R.string.weather_partly_cloudy, WeatherCodeMapper.descriptionRes(2))
        assertEquals("🌤️", WeatherCodeMapper.emoji(2))
    }
    @Test
    fun `weather code 2 at night is cloud emoji`() {
        assertEquals("☁️", WeatherCodeMapper.emoji(2, isDay = false))
    }
    @Test
    fun `weather code 3 is overcast`() {
        assertEquals(R.string.weather_overcast, WeatherCodeMapper.descriptionRes(3))
        assertEquals("☁️", WeatherCodeMapper.emoji(3))
    }
    @Test
    fun `weather code 45 is fog`() {
        assertEquals(R.string.weather_fog, WeatherCodeMapper.descriptionRes(45))
        assertEquals("🌫️", WeatherCodeMapper.emoji(45))
    }
    @Test
    fun `weather code 51 is drizzle`() {
        assertEquals(R.string.weather_drizzle, WeatherCodeMapper.descriptionRes(51))
        assertEquals("🌦️", WeatherCodeMapper.emoji(51))
    }
    @Test
    fun `weather code 56 is freezing drizzle`() {
        assertEquals(R.string.weather_freezing_drizzle, WeatherCodeMapper.descriptionRes(56))
        assertEquals("🌦️", WeatherCodeMapper.emoji(56))
    }
    @Test
    fun `weather code 61 is rain`() {
        assertEquals(R.string.weather_rain, WeatherCodeMapper.descriptionRes(61))
        assertEquals("🌧️", WeatherCodeMapper.emoji(61))
    }
    @Test
    fun `weather code 66 is freezing rain`() {
        assertEquals(R.string.weather_freezing_rain, WeatherCodeMapper.descriptionRes(66))
        assertEquals("🌧️", WeatherCodeMapper.emoji(66))
    }
    @Test
    fun `weather code 71 is snow fall`() {
        assertEquals(R.string.weather_snow_fall, WeatherCodeMapper.descriptionRes(71))
        assertEquals("🌨️", WeatherCodeMapper.emoji(71))
    }
    @Test
    fun `weather code 77 is snow grains`() {
        assertEquals(R.string.weather_snow_grains, WeatherCodeMapper.descriptionRes(77))
        assertEquals("🌨️", WeatherCodeMapper.emoji(77))
    }
    @Test
    fun `weather code 80 is rain showers`() {
        assertEquals(R.string.weather_rain_showers, WeatherCodeMapper.descriptionRes(80))
        assertEquals("🌧️", WeatherCodeMapper.emoji(80))
    }
    @Test
    fun `weather code 85 is snow showers`() {
        assertEquals(R.string.weather_snow_showers, WeatherCodeMapper.descriptionRes(85))
        assertEquals("🌨️", WeatherCodeMapper.emoji(85))
    }
    @Test
    fun `weather code 95 is thunderstorm`() {
        assertEquals(R.string.weather_thunderstorm, WeatherCodeMapper.descriptionRes(95))
        assertEquals("⛈️", WeatherCodeMapper.emoji(95))
    }
    @Test
    fun `weather code 96 is thunderstorm with heavy hail`() {
        assertEquals(R.string.weather_thunderstorm_hail, WeatherCodeMapper.descriptionRes(96))
        assertEquals("⛈️", WeatherCodeMapper.emoji(96))
    }
    @Test
    fun `weather code 999 is unknown`() {
        assertEquals(R.string.weather_unknown, WeatherCodeMapper.descriptionRes(999))
        assertEquals("🌤️", WeatherCodeMapper.emoji(999))
    }
}
