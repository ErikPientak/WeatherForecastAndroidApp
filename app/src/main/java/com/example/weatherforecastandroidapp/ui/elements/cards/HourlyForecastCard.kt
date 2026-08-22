package com.example.weatherforecastandroidapp.ui.elements.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecastandroidapp.ui.theme.WeatherForeCastAndroidAppTheme
import com.example.weatherforecastandroidapp.util.WeatherCodeMapper

/**
 * Presentation-only shape for a single hourly-forecast slot. Kept decoupled from
 * `data.model.HourlyEntry` (which uses a `Double` temperature and a raw time string) the same way
 * [MetricCard] is decoupled from its callers — a future mapper (not part of this file) is expected
 * to turn a list of `HourlyEntry` into a list of these, formatting the time label ("Now" for the
 * current hour, plain "HH:mm" otherwise) along the way.
 */
data class HourlyForecastItem(
    val label: String,
    val temperature: Int,
    val weatherCode: Int,
    val isDay: Boolean = true,
)

/**
 * "Hourly Forecast" card: a header above a horizontally scrollable row of hourly entries, each
 * showing a time label, a [WeatherCodeMapper] emoji glyph, and a temperature.
 *
 * The first entry in [items] is treated as "now" and rendered with emphasized (bold, full-emphasis
 * color) label styling versus the regular/muted styling of the rest — matching the reference design
 * where the current hour is visually distinguished from the row of upcoming hours.
 */
@Composable
fun HourlyForecastCard(
    items: List<HourlyForecastItem>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Hourly Forecast",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            itemsIndexed(items) { index, item ->
                HourlyForecastEntry(item = item, isNow = index == 0)
            }
        }
    }
}

@Composable
private fun HourlyForecastEntry(item: HourlyForecastItem, isNow: Boolean) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            // One merged TalkBack node per entry, with an explicit description: the weather
            // condition is only conveyed visually via the emoji glyph below (decorative on its
            // own), so it needs to be spelled out here for accessibility (e.g. "Now, partly
            // cloudy, 24 degrees").
            .semantics(mergeDescendants = true) {
                contentDescription = "${item.label}, " +
                    "${WeatherCodeMapper.description(item.weatherCode)}, " +
                    "${item.temperature} degrees"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = item.label,
            style = if (isNow) {
                MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (isNow) colorScheme.onSurface else colorScheme.onSurfaceVariant,
        )
        Text(
            text = WeatherCodeMapper.emoji(item.weatherCode, item.isDay),
            fontSize = 24.sp,
            // Decorative: the merged contentDescription above already conveys the condition.
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(
            text = "${item.temperature}°",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface,
        )
    }
}

private val SampleItems = listOf(
    HourlyForecastItem(label = "Now", temperature = 24, weatherCode = 1, isDay = true),
    HourlyForecastItem(label = "15:00", temperature = 23, weatherCode = 2, isDay = true),
    HourlyForecastItem(label = "16:00", temperature = 22, weatherCode = 3, isDay = true),
    HourlyForecastItem(label = "17:00", temperature = 21, weatherCode = 3, isDay = true),
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun HourlyForecastCardLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        HourlyForecastCard(items = SampleItems, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun HourlyForecastCardDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        HourlyForecastCard(items = SampleItems, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Light - Single entry", showBackground = true)
@Composable
private fun HourlyForecastCardSingleEntryLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        HourlyForecastCard(
            items = listOf(HourlyForecastItem(label = "Now", temperature = 18, weatherCode = 0, isDay = true)),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Dark - Night hours", showBackground = true)
@Composable
private fun HourlyForecastCardNightDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        HourlyForecastCard(
            items = listOf(
                HourlyForecastItem(label = "Now", temperature = 12, weatherCode = 0, isDay = false),
                HourlyForecastItem(label = "23:00", temperature = 11, weatherCode = 1, isDay = false),
                HourlyForecastItem(label = "00:00", temperature = 10, weatherCode = 3, isDay = false),
                HourlyForecastItem(label = "01:00", temperature = 9, weatherCode = 45, isDay = false),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light - Long scrollable list", showBackground = true)
@Composable
private fun HourlyForecastCardLongListLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        val items = listOf(HourlyForecastItem(label = "Now", temperature = 24, weatherCode = 1)) +
            (13..23).map { hour ->
                HourlyForecastItem(
                    label = "%02d:00".format(hour),
                    temperature = 24 - (hour - 12),
                    weatherCode = if (hour < 18) 2 else 61,
                )
            }
        HourlyForecastCard(items = items, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Dark - Storm conditions", showBackground = true)
@Composable
private fun HourlyForecastCardStormDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        HourlyForecastCard(
            items = listOf(
                HourlyForecastItem(label = "Now", temperature = 19, weatherCode = 95, isDay = true),
                HourlyForecastItem(label = "15:00", temperature = 18, weatherCode = 96, isDay = true),
                HourlyForecastItem(label = "16:00", temperature = 17, weatherCode = 65, isDay = true),
                HourlyForecastItem(label = "17:00", temperature = 16, weatherCode = 71, isDay = true),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
