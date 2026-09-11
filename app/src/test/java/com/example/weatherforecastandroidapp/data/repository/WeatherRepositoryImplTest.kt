package com.example.weatherforecastandroidapp.data.repository

import com.example.weatherforecastandroidapp.data.model.CurrentConditions
import com.example.weatherforecastandroidapp.data.model.DailyEntry
import com.example.weatherforecastandroidapp.data.model.HourlyEntry
import com.example.weatherforecastandroidapp.data.model.WeatherForecast
import com.example.weatherforecastandroidapp.data.remote.WeatherApiService
import com.example.weatherforecastandroidapp.data.remote.dto.CurrentDto
import com.example.weatherforecastandroidapp.data.remote.dto.DailyDto
import com.example.weatherforecastandroidapp.data.remote.dto.HourlyDto
import com.example.weatherforecastandroidapp.data.remote.dto.WeatherForecastResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * See PlacesRepositoryImplTest for the base MockK/coroutines-test patterns (mockk, coEvery,
 * runTest, coVerify, slot/capture) - this class builds on the same idea but focuses on two things
 * that test didn't need to cover:
 *  1. Asserting a DTO-to-domain mapping is fully correct, not just "some result came back".
 *  2. Asserting exception behavior instead of a return value - specifically, that
 *     CancellationException propagates instead of getting wrapped into Result.failure (see the
 *     try/catch block in getForecast() for why that matters).
 */
class WeatherRepositoryImplTest {

    private val weatherApiService = mockk<WeatherApiService>()
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setUp() {
        repository = WeatherRepositoryImpl(weatherApiService)
    }

    /**
     * Stubs weatherApiService.getForecast(...) to return [response] regardless of what arguments
     * it's called with. WeatherRepositoryImpl builds the current/hourly/daily query strings from
     * private companion constants we don't have access to from the test, so we can't (and don't
     * need to) assert on the exact query string values - any() matches whatever they turn out to
     * be. Note: MockK requires that if ANY argument in a call uses a matcher like any(), ALL
     * arguments in that call must also be matchers - you can't mix any() with a literal value in
     * the same stubbed call.
     */
    private fun stubGetForecast(response: WeatherForecastResponse) {
        coEvery {
            weatherApiService.getForecast(
                latitude = any(),
                longitude = any(),
                current = any(),
                hourly = any(),
                daily = any(),
                timezone = any(),
                forecastDays = any(),
            )
        } returns response
    }

    @Test
    fun `getForecast maps the DTO response into the domain model correctly`() = runTest {
        // Arrange: a fake API response with 2 hourly and 2 daily entries (not 1) is deliberate -
        // WeatherRepositoryImpl.toDomain() builds these lists by zipping several parallel arrays
        // together using shared indices (hourly.time[i], hourly.temperature_2m[i], etc). A list of
        // size 1 can't catch an index-alignment bug because there's only one possible index to
        // read; using distinct values per index means a mis-zip (e.g. hourly.time[0] paired with
        // hourly.temperature_2m[1]) would produce a domain object that doesn't match what we
        // assert below, so this test would actually fail if that mapping logic were broken.
        val response = WeatherForecastResponse(
            current = CurrentDto(
                time = "2026-09-11T12:00",
                temperature_2m = 21.5,
                relative_humidity_2m = 60,
                apparent_temperature = 20.0,
                is_day = 1,
                precipitation = 0.0,
                weather_code = 2,
                surface_pressure = 1013.0,
                wind_speed_10m = 12.0,
                wind_direction_10m = 270,
                dew_point_2m = 13.5,
            ),
            hourly = HourlyDto(
                time = listOf("2026-09-11T12:00", "2026-09-11T13:00"),
                temperature_2m = listOf(21.5, 22.0),
                weather_code = listOf(2, 3),
                precipitation_probability = listOf(10, 20),
                uv_index = listOf(4.0, 5.0),
            ),
            daily = DailyDto(
                time = listOf("2026-09-11", "2026-09-12"),
                weather_code = listOf(2, 61),
                temperature_2m_max = listOf(25.0, 22.0),
                temperature_2m_min = listOf(15.0, 14.0),
                precipitation_probability_max = listOf(10, 80),
                sunrise = listOf("2026-09-11T06:30", "2026-09-12T06:31"),
                sunset = listOf("2026-09-11T19:45", "2026-09-12T19:43"),
                uv_index_max = listOf(6.0, 3.0),
                daylight_duration = listOf(47700.0, 47600.0),
            ),
        )
        stubGetForecast(response)

        // Act
        val result = repository.getForecast(latitude = 50.0755, longitude = 14.4378)

        // Assert: build the expected domain object by hand (mirroring toDomain()'s field mapping)
        // and compare full equality via a data class - this only works because WeatherForecast,
        // CurrentConditions, HourlyEntry and DailyEntry are all data classes with structural
        // equals(). This is stricter than checking individual fields one by one: if toDomain()
        // ever adds a new field to a domain model and forgets to map it, this assertion catches it
        // immediately (the actual object won't equal the expected one) instead of silently passing.
        val expected = WeatherForecast(
            current = CurrentConditions(
                time = "2026-09-11T12:00",
                temperature = 21.5,
                apparentTemperature = 20.0,
                humidity = 60,
                windSpeed = 12.0,
                windDirection = 270,
                pressure = 1013.0,
                isDay = true, // is_day == 1 maps to true
                weatherCode = 2,
                dewPoint = 13.5,
            ),
            hourly = listOf(
                HourlyEntry(time = "2026-09-11T12:00", temperature = 21.5, precipitationProbability = 10, weatherCode = 2, uvIndex = 4.0),
                HourlyEntry(time = "2026-09-11T13:00", temperature = 22.0, precipitationProbability = 20, weatherCode = 3, uvIndex = 5.0),
            ),
            daily = listOf(
                DailyEntry(date = "2026-09-11", weatherCode = 2, tempMax = 25.0, tempMin = 15.0, precipitationProbabilityMax = 10, sunrise = "2026-09-11T06:30", sunset = "2026-09-11T19:45", uvIndexMax = 6.0, daylightDuration = 47700.0),
                DailyEntry(date = "2026-09-12", weatherCode = 61, tempMax = 22.0, tempMin = 14.0, precipitationProbabilityMax = 80, sunrise = "2026-09-12T06:31", sunset = "2026-09-12T19:43", uvIndexMax = 3.0, daylightDuration = 47600.0),
            ),
        )
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getForecast is_day 0 maps to isDay false`() = runTest {
        // A narrow, targeted test for the one bit of non-trivial logic in the current-conditions
        // mapping: is_day is an Int (0 or 1) from the API, but the domain model wants a Boolean.
        // The happy-path test above only exercises is_day = 1; this covers the other branch.
        val response = WeatherForecastResponse(
            current = CurrentDto(
                time = "t", temperature_2m = 0.0, relative_humidity_2m = 0,
                apparent_temperature = 0.0, is_day = 0, precipitation = 0.0,
                weather_code = 0, surface_pressure = 0.0, wind_speed_10m = 0.0,
                wind_direction_10m = 0, dew_point_2m = 0.0,
            ),
            hourly = HourlyDto(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
            daily = DailyDto(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
        )
        stubGetForecast(response)

        val result = repository.getForecast(latitude = 0.0, longitude = 0.0)

        assertEquals(false, result.getOrNull()?.current?.isDay)
    }

    @Test
    fun `getForecast wraps a network failure in Result failure`() = runTest {
        val networkError = IOException("no connection")
        coEvery {
            weatherApiService.getForecast(any(), any(), any(), any(), any(), any(), any())
        } throws networkError

        val result = repository.getForecast(latitude = 50.0755, longitude = 14.4378)

        assertTrue(result.isFailure)
        assertEquals(networkError, result.exceptionOrNull())
    }

    @Test
    fun `getForecast rethrows CancellationException instead of wrapping it in Result`() = runTest {
        coEvery {
            weatherApiService.getForecast(any(), any(), any(), any(), any(), any(), any())
        } throws CancellationException("coroutine was cancelled")

        // We can't use assertThrows here in the usual way because getForecast is a suspend
        // function being called inside runTest's own coroutine - letting a real
        // CancellationException escape uncaught would cancel that coroutine rather than cleanly
        // "fail the assertion". So we catch it ourselves and assert it was actually thrown; this
        // is one of the few legitimate reasons to manually catch CancellationException instead of
        // always letting it propagate.
        var caught: CancellationException? = null
        try {
            repository.getForecast(latitude = 50.0755, longitude = 14.4378)
        } catch (e: CancellationException) {
            caught = e
        }

        assertNotNull("Expected CancellationException to propagate out of getForecast", caught)
    }
}
