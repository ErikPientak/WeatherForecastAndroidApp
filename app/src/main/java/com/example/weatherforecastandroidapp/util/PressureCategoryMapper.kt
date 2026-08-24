package com.example.weatherforecastandroidapp.util

import androidx.annotation.StringRes
import com.example.weatherforecastandroidapp.R

/**
 * Maps a surface pressure reading (hPa) to a Low/Normal/High category. This is an absolute
 * categorization, not a rising/falling trend — the app doesn't store prior readings to compare
 * against yet.
 */
object PressureCategoryMapper {

    @StringRes
    fun categoryRes(pressureHpa: Double): Int = when {
        pressureHpa < 1009.0 -> R.string.pressure_category_low
        pressureHpa <= 1015.0 -> R.string.pressure_category_normal
        else -> R.string.pressure_category_high
    }
}
