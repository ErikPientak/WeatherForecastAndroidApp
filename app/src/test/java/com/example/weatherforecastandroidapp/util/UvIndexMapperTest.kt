package com.example.weatherforecastandroidapp.util

import com.example.weatherforecastandroidapp.R
import org.junit.Assert.assertEquals
import org.junit.Test

class UvIndexMapperTest {

    @Test
    fun `uv index 0 is low`() {
        assertEquals(R.string.uv_category_low, UvIndexMapper.categoryRes(0))
    }

    @Test
    fun `uv index at low-moderate boundary (2) is still low`() {
        assertEquals(R.string.uv_category_low, UvIndexMapper.categoryRes(2))
    }

    @Test
    fun `uv index just above low-moderate boundary (3) is moderate`() {
        assertEquals(R.string.uv_category_moderate, UvIndexMapper.categoryRes(3))
    }

    @Test
    fun `uv index at moderate-high boundary (5) is still moderate`() {
        assertEquals(R.string.uv_category_moderate, UvIndexMapper.categoryRes(5))
    }

    @Test
    fun `uv index just above moderate-high boundary (6) is high`() {
        assertEquals(R.string.uv_category_high, UvIndexMapper.categoryRes(6))
    }

    @Test
    fun `uv index at high-very high boundary (7) is still high`() {
        assertEquals(R.string.uv_category_high, UvIndexMapper.categoryRes(7))
    }

    @Test
    fun `uv index just above high-very high boundary (8) is very high`() {
        assertEquals(R.string.uv_category_very_high, UvIndexMapper.categoryRes(8))
    }

    @Test
    fun `uv index at very high-extreme boundary (10) is still very high`() {
        assertEquals(R.string.uv_category_very_high, UvIndexMapper.categoryRes(10))
    }

    @Test
    fun `uv index just above very high-extreme boundary (11) is extreme`() {
        assertEquals(R.string.uv_category_extreme, UvIndexMapper.categoryRes(11))
    }

    @Test
    fun `very large uv index is extreme`() {
        assertEquals(R.string.uv_category_extreme, UvIndexMapper.categoryRes(25))
    }

    @Test
    fun `negative uv index falls back to low`() {
        // categoryRes has no lower bound check, so any value <= 2 (including negatives) is "low".
        // This documents that current behavior rather than asserting it's necessarily correct -
        // a negative UV index shouldn't occur from the API, but nothing here guards against it.
        assertEquals(R.string.uv_category_low, UvIndexMapper.categoryRes(-1))
    }
}
