package com.example.weatherforecastandroidapp.ui.elements.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.ui.theme.DisplayLargeMobile
import com.example.weatherforecastandroidapp.ui.theme.WeatherForeCastAndroidAppTheme
import com.example.weatherforecastandroidapp.util.WeatherCodeMapper

/**
 * Compact current-weather summary card: icon, temperature, condition text, and a high/low pill,
 * centered on a vertical gradient background. The gradient is built from theme color-scheme
 * roles (not hardcoded hex) so it follows the active light/dark theme automatically.
 *
 * The icon is the emoji from [WeatherCodeMapper] as a stand-in until a proper vector weather
 * icon set exists in the project.
 */
@Composable
fun HomeCard(
    temperature: Int,
    weatherCode: Int,
    isDay: Boolean = true,
    highTemperature: Int,
    lowTemperature: Int,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val gradientColors = if (isDarkTheme) {
        listOf(colorScheme.primaryContainer, colorScheme.surface)
    } else {
        listOf(colorScheme.surface, colorScheme.primaryFixed)
    }
    val highLowDescription = stringResource(
        R.string.home_card_high_low_description, highTemperature, lowTemperature
    )

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = WeatherCodeMapper.emoji(weatherCode, isDay),
            fontSize = 56.sp,
            // Decorative: the condition text below already conveys this information to TalkBack.
            modifier = Modifier.clearAndSetSemantics {},
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = "$temperature°",
            style = DisplayLargeMobile,
            color = colorScheme.onSurface,
        )
        Text(
            text = stringResource(WeatherCodeMapper.descriptionRes(weatherCode)),
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.onSurfaceVariant,
        )
        Surface(
            shape = MaterialTheme.shapes.small,
            color = colorScheme.surfaceContainerHigh,
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = highLowDescription
            },
        ) {
            Text(
                text = "H: $highTemperature° • L: $lowTemperature°",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun HomeCardLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        HomeCard(
            isDarkTheme = false,
            temperature = 24,
            weatherCode = 2,
            isDay = true,
            highTemperature = 26,
            lowTemperature = 18,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun HomeCardDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        HomeCard(
            isDarkTheme = true,
            temperature = 24,
            weatherCode = 2,
            isDay = true,
            highTemperature = 26,
            lowTemperature = 18,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}

@Preview(name = "Light - Clear day", showBackground = true)
@Composable
private fun HomeCardClearDayPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        HomeCard(
            isDarkTheme = false,
            temperature = 31,
            weatherCode = 0,
            isDay = true,
            highTemperature = 33,
            lowTemperature = 22,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}

@Preview(name = "Dark - Clear night", showBackground = true)
@Composable
private fun HomeCardClearNightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        HomeCard(
            isDarkTheme = true,
            temperature = 12,
            weatherCode = 0,
            isDay = false,
            highTemperature = 19,
            lowTemperature = 9,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}

@Preview(name = "Light - Rain", showBackground = true)
@Composable
private fun HomeCardRainPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        HomeCard(
            isDarkTheme = false,
            temperature = 16,
            weatherCode = 63,
            isDay = true,
            highTemperature = 18,
            lowTemperature = 13,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}

@Preview(name = "Dark - Thunderstorm", showBackground = true)
@Composable
private fun HomeCardThunderstormPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        HomeCard(
            isDarkTheme = true,
            temperature = 21,
            weatherCode = 95,
            isDay = true,
            highTemperature = 25,
            lowTemperature = 17,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}

@Preview(name = "Light - Snow", showBackground = true)
@Composable
private fun HomeCardSnowPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        HomeCard(
            isDarkTheme = false,
            temperature = -3,
            weatherCode = 73,
            isDay = true,
            highTemperature = 1,
            lowTemperature = -6,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}

@Preview(name = "Light - Fog", showBackground = true)
@Composable
private fun HomeCardFogPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        HomeCard(
            isDarkTheme = false,
            temperature = 9,
            weatherCode = 45,
            isDay = true,
            highTemperature = 11,
            lowTemperature = 7,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}
