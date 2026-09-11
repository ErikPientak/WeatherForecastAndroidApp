package com.example.weatherforecastandroidapp.util

import com.example.weatherforecastandroidapp.R
import org.junit.Assert.assertEquals
import org.junit.Test

class PressureCategoryMapperTest {
    @Test
    fun `Surface pressure is low`() {
        assertEquals(R.string.pressure_category_low, PressureCategoryMapper.categoryRes(1000.0))
    }

    @Test
    fun `Surface pressure is normal`() {
        assertEquals(R.string.pressure_category_normal, PressureCategoryMapper.categoryRes(1012.0))
    }

    @Test
    fun `Surface pressure is high`() {
        assertEquals(R.string.pressure_category_high, PressureCategoryMapper.categoryRes(1020.0))
    }

    @Test
    fun `pressure just below low-normal boundary is low`() {
        assertEquals(R.string.pressure_category_low, PressureCategoryMapper.categoryRes(1008.99))
    }

    @Test
    fun `pressure at low-normal boundary is normal`() {
        assertEquals(R.string.pressure_category_normal, PressureCategoryMapper.categoryRes(1009.0))
    }

    @Test
    fun `pressure at normal-high boundary is still normal`() {
        assertEquals(R.string.pressure_category_normal, PressureCategoryMapper.categoryRes(1015.0))
    }

    @Test
    fun `pressure just above normal-high boundary is high`() {
        assertEquals(R.string.pressure_category_high, PressureCategoryMapper.categoryRes(1015.01))
    }
}
