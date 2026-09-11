package com.example.weatherforecastandroidapp.util

import com.example.weatherforecastandroidapp.R
import org.junit.Assert.assertEquals
import org.junit.Test

class WindDirectionMapperTest {
    @Test
    fun `wind direction 0 is north`() {
        assertEquals(R.string.wind_direction_n, WindDirectionMapper.compassRes(0))
    }

    @Test
    fun `wind direction 90 is east`() {
        assertEquals(R.string.wind_direction_e, WindDirectionMapper.compassRes(90))
    }

    @Test
    fun `wind direction 180 is south`() {
        assertEquals(R.string.wind_direction_s, WindDirectionMapper.compassRes(180))
    }

    @Test
    fun `wind direction 270 is west`() {
        assertEquals(R.string.wind_direction_w, WindDirectionMapper.compassRes(270))
    }

    @Test
    fun `wind direction 360 is north again`() {
        assertEquals(R.string.wind_direction_n, WindDirectionMapper.compassRes(360))
    }

    @Test
    fun `wind direction 45 is north-east`() {
        assertEquals(R.string.wind_direction_ne, WindDirectionMapper.compassRes(45))
    }

    @Test
    fun `wind direction 315 is north-west`() {
        assertEquals(R.string.wind_direction_nw, WindDirectionMapper.compassRes(315))
    }

    @Test
    fun `wind direction 135 is south-east`() {
        assertEquals(R.string.wind_direction_se, WindDirectionMapper.compassRes(135))
    }

    @Test
    fun `wind direction 225 is south-west`() {
        assertEquals(R.string.wind_direction_sw, WindDirectionMapper.compassRes(225))
    }

    @Test
    fun `wind direction 359 wraps to north`() {
        assertEquals(R.string.wind_direction_n, WindDirectionMapper.compassRes(359))
    }

    @Test
    fun `negative wind direction wraps around to north`() {
        // -10 degrees should behave the same as 350 degrees (still within north's wedge).
        assertEquals(R.string.wind_direction_n, WindDirectionMapper.compassRes(-10))
    }
}