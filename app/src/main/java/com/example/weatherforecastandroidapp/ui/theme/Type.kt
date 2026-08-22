package com.example.weatherforecastandroidapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.weatherforecastandroidapp.R

// "Atmospheric Material" type scale, using the actual "Roboto Flex" variable font bundled at
// res/font/roboto_flex.ttf (single variable file; each weight below just dials in a different
// value on its "wght" axis rather than needing separate font files per weight).
@OptIn(ExperimentalTextApi::class)
private fun robotoFlex(weight: Int) = FontFamily(
    Font(R.font.roboto_flex, variationSettings = FontVariation.Settings(FontVariation.weight(weight)))
)

private val RobotoFlexLight = robotoFlex(300)
private val RobotoFlexRegular = robotoFlex(400)
private val RobotoFlexMedium = robotoFlex(500)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = RobotoFlexLight,
        fontWeight = FontWeight.Light,
        fontSize = 96.sp,
        lineHeight = 112.sp,
        letterSpacing = (-1.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = RobotoFlexRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = RobotoFlexRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoFlexRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = RobotoFlexMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoFlexMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoFlexRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoFlexRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = RobotoFlexMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// Not a standard Material3 Typography slot — per the design spec, the current-temperature
// display swaps to this smaller scale on mobile viewports instead of displayLarge.
val DisplayLargeMobile = TextStyle(
    fontFamily = RobotoFlexLight,
    fontWeight = FontWeight.Light,
    fontSize = 72.sp,
    lineHeight = 80.sp,
)
