package com.example.weatherforecastandroidapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// "Midnight Atmospheric" — DESIGNDARK.txt
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurfaceTint,
    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    primaryFixed = DarkPrimaryFixed,
    primaryFixedDim = DarkPrimaryFixedDim,
    onPrimaryFixed = DarkOnPrimaryFixed,
    onPrimaryFixedVariant = DarkOnPrimaryFixedVariant,
    secondaryFixed = DarkSecondaryFixed,
    secondaryFixedDim = DarkSecondaryFixedDim,
    onSecondaryFixed = DarkOnSecondaryFixed,
    onSecondaryFixedVariant = DarkOnSecondaryFixedVariant,
    tertiaryFixed = DarkTertiaryFixed,
    tertiaryFixedDim = DarkTertiaryFixedDim,
    onTertiaryFixed = DarkOnTertiaryFixed,
    onTertiaryFixedVariant = DarkOnTertiaryFixedVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
)

@Composable
fun WeatherForeCastAndroidAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default: dynamic (wallpaper-based) color would override the "Atmospheric Material"
    // brand palette on Android 12+ devices, which defeats the point of a designed theme.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}