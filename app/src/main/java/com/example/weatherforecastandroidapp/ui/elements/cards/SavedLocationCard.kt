package com.example.weatherforecastandroidapp.ui.elements.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.ui.theme.NightCardOnSurface
import com.example.weatherforecastandroidapp.ui.theme.NightCardOnSurfaceVariant
import com.example.weatherforecastandroidapp.ui.theme.NightCardSurface
import com.example.weatherforecastandroidapp.ui.theme.WeatherForeCastAndroidAppTheme
import com.example.weatherforecastandroidapp.util.WeatherCodeMapper

/**
 * Presentation-only shape for a single saved-place tile. Kept decoupled from
 * `data.model.SavedPlace` (which has no weather fields at all yet) the same way
 * [HourlyForecastItem] is decoupled from `HourlyEntry` — a future mapper is expected to combine a
 * `SavedPlace` with a per-place forecast fetch (and resolve its local time from `latitude`/
 * `longitude`) to build one of these; that mapping is not part of this file.
 */
data class SavedLocationWeather(
    val cityName: String,
    // Pre-formatted by the caller (e.g. "04:20 AM"), same reasoning as WeeklyForecastDay's
    // dayLabel: this composable doesn't know the place's timezone, so it can't format a raw
    // Instant/LocalDateTime itself.
    val localTime: String,
    val temperature: Int,
    val weatherCode: Int,
    val isDay: Boolean = true,
    val highTemperature: Int,
    val lowTemperature: Int,
)

/**
 * Saved-location grid tile: city name + local time top-left, a [WeatherCodeMapper] emoji
 * top-right, a big current temperature bottom-left, and an uppercase condition label + high/low
 * line bottom-right. Designed to sit in a `LazyVerticalGrid(columns = 2)` on the (not yet built)
 * Saved Places screen, matching the reference mockup's 2x2 grid — but the grid wiring itself is a
 * separate task; this file only builds the standalone tile, same as [WeeklyForecastCard] was
 * built standalone before being wired in.
 *
 * ### Background color strategy
 * Unlike every other card in `ui/elements/cards/` (which use a single flat
 * `colorScheme.surfaceContainer` that only changes with the app's own light/dark theme), this
 * tile's background is driven by **weather condition + day/night**, independent of the app theme
 * — confirmed with the user against the reference mockup, which shows four tiles on screen at
 * once with four different fills (a dark night tile next to light sunny/cloudy day tiles). To do
 * this without inventing an unrelated hardcoded palette, it reuses this app's existing M3 "Fixed"
 * color roles, whose whole purpose is a stable, theme-independent-looking surface + two text
 * tones ([androidx.compose.material3.ColorScheme.tertiaryFixed]/`onTertiaryFixed`/
 * `onTertiaryFixedVariant`, etc.):
 * - Night (`isDay == false`, any weather code) -> [NightCardSurface] / [NightCardOnSurface] /
 *   [NightCardOnSurfaceVariant] — new tokens (see `Color.kt`), since none of the app's existing
 *   Fixed roles render dark. Night always wins over condition, matching the mockup's rainy-but-
 *   dark Tokyo tile.
 * - Clear/mostly clear day (codes 0, 1) -> `tertiaryFixed` family ("Sunlit Gold" — already the
 *   spec's reserved color for clear skies, see [PrecipitationChanceGraphCard]'s doc comment on the
 *   Rain Indigo fallback decision for the sibling precedent).
 * - Cloudy/foggy day (codes 2, 3, 45, 48) -> `secondaryFixed` family.
 * - Everything else (drizzle/rain/snow/thunderstorm) -> `primaryFixed` family, matching this
 *   project's confirmed "precipitation falls back to Primary" decision.
 *
 * The icon stays the existing [WeatherCodeMapper] emoji convention (no new vector asset), and
 * there's deliberately no bookmark/saved indicator here — every tile in this grid is already a
 * saved place, so the marker would be redundant; an unsave affordance is left for a future
 * screen-level concern.
 */
@Composable
fun SavedLocationCard(
    weather: SavedLocationWeather,
    modifier: Modifier = Modifier,
) {
    val colors = savedLocationCardColors(weatherCode = weather.weatherCode, isDay = weather.isDay)
    val conditionLabel = stringResource(WeatherCodeMapper.descriptionRes(weather.weatherCode)).uppercase()
    val cardDescription = stringResource(
        R.string.saved_location_card_description,
        weather.cityName,
        weather.localTime,
        conditionLabel,
        weather.temperature,
        weather.highTemperature,
        weather.lowTemperature,
    )

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.background)
            .padding(16.dp)
            // One merged TalkBack node per tile, same reasoning as HourlyForecastEntry/
            // WeeklyForecastRow: the emoji glyph is decorative on its own, so the condition is
            // spelled out here alongside everything else in one announcement.
            .semantics(mergeDescendants = true) {
                contentDescription = cardDescription
            },
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = weather.cityName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface,
                )
                Text(
                    text = weather.localTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Text(
                text = WeatherCodeMapper.emoji(weather.weatherCode, weather.isDay),
                fontSize = 26.sp,
                // Decorative: the merged contentDescription above already conveys the condition.
                modifier = Modifier.clearAndSetSemantics {},
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "${weather.temperature}°",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface,
            )
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = conditionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
                Text(
                    text = "H:${weather.highTemperature}° L:${weather.lowTemperature}°",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

/** Background + two text tones for one [SavedLocationCard] tile — see its doc comment. */
private data class SavedLocationCardColors(
    val background: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
)

@Composable
private fun savedLocationCardColors(weatherCode: Int, isDay: Boolean): SavedLocationCardColors {
    if (!isDay) {
        return SavedLocationCardColors(
            background = NightCardSurface,
            onSurface = NightCardOnSurface,
            onSurfaceVariant = NightCardOnSurfaceVariant,
        )
    }

    val colorScheme = MaterialTheme.colorScheme
    return when (weatherCode) {
        0, 1 -> SavedLocationCardColors(
            background = colorScheme.tertiaryFixed,
            onSurface = colorScheme.onTertiaryFixed,
            onSurfaceVariant = colorScheme.onTertiaryFixedVariant,
        )
        2, 3, 45, 48 -> SavedLocationCardColors(
            background = colorScheme.secondaryFixed,
            onSurface = colorScheme.onSecondaryFixed,
            onSurfaceVariant = colorScheme.onSecondaryFixedVariant,
        )
        else -> SavedLocationCardColors(
            background = colorScheme.primaryFixed,
            onSurface = colorScheme.onPrimaryFixed,
            onSurfaceVariant = colorScheme.onPrimaryFixedVariant,
        )
    }
}

private val SampleTokyo = SavedLocationWeather(
    cityName = "Tokyo",
    localTime = "04:20 AM",
    temperature = 22,
    weatherCode = 63,
    isDay = false,
    highTemperature = 25,
    lowTemperature = 19,
)
private val SampleNewYork = SavedLocationWeather(
    cityName = "New York",
    localTime = "03:20 PM",
    temperature = 28,
    weatherCode = 0,
    isDay = true,
    highTemperature = 30,
    lowTemperature = 22,
)
private val SampleLondon = SavedLocationWeather(
    cityName = "London",
    localTime = "08:20 PM",
    temperature = 15,
    weatherCode = 3,
    isDay = true,
    highTemperature = 18,
    lowTemperature = 12,
)
private val SampleSydney = SavedLocationWeather(
    cityName = "Sydney",
    localTime = "05:20 AM",
    temperature = 24,
    weatherCode = 1,
    isDay = true,
    highTemperature = 26,
    lowTemperature = 18,
)

@Composable
private fun SavedLocationCardGridPreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SavedLocationCard(weather = SampleTokyo, modifier = Modifier.weight(1f))
            SavedLocationCard(weather = SampleNewYork, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SavedLocationCard(weather = SampleLondon, modifier = Modifier.weight(1f))
            SavedLocationCard(weather = SampleSydney, modifier = Modifier.weight(1f))
        }
    }
}

@Preview(name = "Grid - Light (reference mockup)", showBackground = true)
@Composable
private fun SavedLocationCardGridLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        SavedLocationCardGridPreviewContent()
    }
}

@Preview(name = "Grid - Dark", showBackground = true)
@Composable
private fun SavedLocationCardGridDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        SavedLocationCardGridPreviewContent()
    }
}

@Preview(name = "Tile - Night rain (Tokyo)", showBackground = true)
@Composable
private fun SavedLocationCardNightRainPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        SavedLocationCard(weather = SampleTokyo, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Tile - Sunny day", showBackground = true)
@Composable
private fun SavedLocationCardSunnyPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        SavedLocationCard(weather = SampleNewYork, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Tile - Cloudy day", showBackground = true)
@Composable
private fun SavedLocationCardCloudyPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        SavedLocationCard(weather = SampleLondon, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Tile - Dark app theme, sunny condition", showBackground = true)
@Composable
private fun SavedLocationCardSunnyDarkAppThemePreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        SavedLocationCard(weather = SampleSydney, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Tile - Thunderstorm, extreme temps", showBackground = true)
@Composable
private fun SavedLocationCardThunderstormPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        SavedLocationCard(
            weather = SavedLocationWeather(
                cityName = "Miami",
                localTime = "06:45 PM",
                temperature = 29,
                weatherCode = 95,
                isDay = true,
                highTemperature = 33,
                lowTemperature = 26,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Tile - Fog, negative temps, long name", showBackground = true)
@Composable
private fun SavedLocationCardFogLongNamePreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        SavedLocationCard(
            weather = SavedLocationWeather(
                cityName = "Reykjavik",
                localTime = "11:10 AM",
                temperature = -4,
                weatherCode = 45,
                isDay = true,
                highTemperature = -1,
                lowTemperature = -8,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Tile - Clear night, dark app theme", showBackground = true)
@Composable
private fun SavedLocationCardClearNightDarkAppThemePreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        SavedLocationCard(
            weather = SavedLocationWeather(
                cityName = "Prague",
                localTime = "11:55 PM",
                temperature = 8,
                weatherCode = 0,
                isDay = false,
                highTemperature = 14,
                lowTemperature = 6,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
