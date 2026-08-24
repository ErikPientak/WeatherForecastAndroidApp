package com.example.weatherforecastandroidapp.util

import androidx.annotation.StringRes
import com.example.weatherforecastandroidapp.R

/**
 * Maps a numeric UV index to the standard WHO UV index category.
 */
object UvIndexMapper {

    @StringRes
    fun categoryRes(uvIndex: Int): Int = when {
        uvIndex <= 2 -> R.string.uv_category_low
        uvIndex <= 5 -> R.string.uv_category_moderate
        uvIndex <= 7 -> R.string.uv_category_high
        uvIndex <= 10 -> R.string.uv_category_very_high
        else -> R.string.uv_category_extreme
    }
}
