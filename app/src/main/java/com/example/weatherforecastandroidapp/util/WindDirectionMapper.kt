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

    // Each point covers a 45-degree wedge centered on its heading (e.g. NE = 22.5-67.5 degrees).
    private val COMPASS_POINTS = listOf(
        R.string.wind_direction_n,  // 337.5 - 22.5
        R.string.wind_direction_ne, // 22.5 - 67.5
        R.string.wind_direction_e,  // 67.5 - 112.5
        R.string.wind_direction_se, // 112.5 - 157.5
        R.string.wind_direction_s,  // 157.5 - 202.5
        R.string.wind_direction_sw, // 202.5 - 247.5
        R.string.wind_direction_w,  // 247.5 - 292.5
        R.string.wind_direction_nw, // 292.5 - 337.5
    )
}
