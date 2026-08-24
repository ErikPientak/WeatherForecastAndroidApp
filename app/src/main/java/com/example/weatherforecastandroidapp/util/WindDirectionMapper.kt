package com.example.weatherforecastandroidapp.util

import androidx.annotation.StringRes
import com.example.weatherforecastandroidapp.R

/**
 * Maps a wind direction in degrees (0-359, meteorological convention: the direction the wind is
 * blowing *from*) to an 8-point compass abbreviation.
 */
object WindDirectionMapper {

    @StringRes
    fun compassRes(degrees: Int): Int {
        val normalized = ((degrees % 360) + 360) % 360
        val index = ((normalized + 22.5) / 45.0).toInt() % 8
        return COMPASS_POINTS[index]
    }

    private val COMPASS_POINTS = listOf(
        R.string.wind_direction_n,
        R.string.wind_direction_ne,
        R.string.wind_direction_e,
        R.string.wind_direction_se,
        R.string.wind_direction_s,
        R.string.wind_direction_sw,
        R.string.wind_direction_w,
        R.string.wind_direction_nw,
    )
}
