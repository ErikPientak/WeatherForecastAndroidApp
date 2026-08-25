package com.example.weatherforecastandroidapp.ui.elements.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.ui.theme.WeatherForeCastAndroidAppTheme
import com.example.weatherforecastandroidapp.util.WeatherCodeMapper

/**
 * Presentation-only shape for a single day on the 7-day forecast card. Kept decoupled from
 * `data.model.DailyEntry` the same way [HourlyForecastItem] is decoupled from `HourlyEntry` — a
 * mapper from `DailyEntry` to a list of these is a future step, not part of this file.
 */
data class WeeklyForecastDay(
    val dayLabel: String,
    val weatherCode: Int,
    val precipitationChance: Int, // 0-100
    val lowTemperature: Int,
    val highTemperature: Int,
)

private val DayLabelWidth = 32.dp
private val EmojiWidth = 24.dp
private val PercentageWidth = 36.dp
private val TemperatureWidth = 34.dp
private val BarHeight = 6.dp

// Precipitation chances below this are treated as "no rain" and hidden, matching the reference
// design's dry-day rows which show no percentage at all.
private const val NegligiblePrecipitationThreshold = 5

/**
 * "7-Day Forecast" card: a header above one row per day, each showing a short weekday label, a
 * [WeatherCodeMapper] emoji, an (optionally hidden) precipitation percentage, and a low/high
 * temperature range rendered as a hand-drawn bar (Canvas, no charting library — same approach as
 * [PrecipitationChanceGraphCard]'s curve).
 *
 * The bar is normalized across the *whole* [days] list (a shared week-wide min/max), not per row,
 * so each day's filled segment is positioned relative to the coldest/hottest point of the week
 * rather than to its own low/high — making the bars comparable across rows.
 */
@Composable
fun WeeklyForecastCard(
    days: List<WeeklyForecastDay>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val weekMin = days.minOfOrNull { it.lowTemperature } ?: 0
    val weekMax = days.maxOfOrNull { it.highTemperature } ?: 0

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "📅",
                fontSize = 20.sp,
                // Decorative: the title text right next to it already conveys what this card is.
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(
                text = stringResource(R.string.weekly_forecast_title),
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            days.forEach { day ->
                WeeklyForecastRow(day = day, weekMin = weekMin, weekMax = weekMax)
            }
        }
    }
}

@Composable
private fun WeeklyForecastRow(day: WeeklyForecastDay, weekMin: Int, weekMax: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val entryDescription = stringResource(
        R.string.weekly_forecast_entry_description,
        day.dayLabel,
        stringResource(WeatherCodeMapper.descriptionRes(day.weatherCode)),
        day.lowTemperature,
        day.highTemperature,
    )
    val showPrecipitation = day.precipitationChance >= NegligiblePrecipitationThreshold

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // One merged TalkBack node per row, with an explicit description: the weather
            // condition is only conveyed visually via the emoji glyph (decorative on its own) and
            // the temperature range is only conveyed visually via the bar (also decorative), so
            // both need to be spelled out here (e.g. "Mon, partly cloudy, low 18 degrees, high 26
            // degrees").
            .semantics(mergeDescendants = true) {
                contentDescription = entryDescription
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = day.dayLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurface,
            modifier = Modifier.width(DayLabelWidth),
        )
        Text(
            text = WeatherCodeMapper.emoji(day.weatherCode, isDay = true),
            fontSize = 18.sp,
            // Decorative: the merged contentDescription above already conveys the condition.
            modifier = Modifier.width(EmojiWidth).clearAndSetSemantics {},
        )
        Text(
            // The width below is fixed regardless of whether the text is shown, so hiding
            // negligible precipitation doesn't shift the low-temp/bar/high-temp columns.
            text = if (showPrecipitation) "${day.precipitationChance}%" else "",
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.width(PercentageWidth),
        )
        Text(
            text = "${day.lowTemperature}°",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(TemperatureWidth),
        )
        TemperatureRangeBar(
            low = day.lowTemperature,
            high = day.highTemperature,
            weekMin = weekMin,
            weekMax = weekMax,
            trackColor = colorScheme.surfaceVariant,
            fillColor = colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .height(BarHeight)
                // Decorative: the low/high temp text on either side already conveys the range.
                .clearAndSetSemantics {},
        )
        Text(
            text = "${day.highTemperature}°",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurface,
            modifier = Modifier.width(TemperatureWidth),
        )
    }
}

/**
 * Hand-drawn temperature-range bar: a full-width rounded-rect track, with a rounded-rect fill
 * segment spanning from [low] to [high], both positioned as fractions of the [weekMin]..[weekMax]
 * range (not [low]..[high] itself — see [WeeklyForecastCard]'s doc comment).
 */
@Composable
private fun TemperatureRangeBar(
    low: Int,
    high: Int,
    weekMin: Int,
    weekMax: Int,
    trackColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
        drawRoundRect(color = trackColor, cornerRadius = cornerRadius)

        val range = (weekMax - weekMin).toFloat()
        val startFraction: Float
        val endFraction: Float
        if (range <= 0f) {
            // Every day has the same low/high across the whole week (or a single-day list) —
            // fall back to a full-width bar instead of dividing by zero.
            startFraction = 0f
            endFraction = 1f
        } else {
            startFraction = ((low - weekMin) / range).coerceIn(0f, 1f)
            endFraction = ((high - weekMin) / range).coerceIn(0f, 1f)
        }

        val fillStartX = size.width * startFraction
        val fillEndX = size.width * endFraction
        drawRoundRect(
            color = fillColor,
            topLeft = Offset(fillStartX, 0f),
            size = Size(fillEndX - fillStartX, size.height),
            cornerRadius = cornerRadius,
        )
    }
}

private val SampleDays = listOf(
    WeeklyForecastDay(dayLabel = "Mon", weatherCode = 2, precipitationChance = 15, lowTemperature = 18, highTemperature = 26),
    WeeklyForecastDay(dayLabel = "Tue", weatherCode = 65, precipitationChance = 75, lowTemperature = 16, highTemperature = 22),
    WeeklyForecastDay(dayLabel = "Wed", weatherCode = 0, precipitationChance = 2, lowTemperature = 20, highTemperature = 28),
    WeeklyForecastDay(dayLabel = "Thu", weatherCode = 3, precipitationChance = 20, lowTemperature = 18, highTemperature = 24),
    WeeklyForecastDay(dayLabel = "Fri", weatherCode = 51, precipitationChance = 30, lowTemperature = 17, highTemperature = 23),
    WeeklyForecastDay(dayLabel = "Sat", weatherCode = 95, precipitationChance = 60, lowTemperature = 15, highTemperature = 20),
    WeeklyForecastDay(dayLabel = "Sun", weatherCode = 0, precipitationChance = 3, lowTemperature = 19, highTemperature = 27),
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun WeeklyForecastCardLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        WeeklyForecastCard(days = SampleDays, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun WeeklyForecastCardDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        WeeklyForecastCard(days = SampleDays, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Light - Single day", showBackground = true)
@Composable
private fun WeeklyForecastCardSingleDayLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        WeeklyForecastCard(
            days = listOf(
                WeeklyForecastDay(dayLabel = "Mon", weatherCode = 1, precipitationChance = 10, lowTemperature = 18, highTemperature = 26),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Dark - Equal temps (zero-range guard)", showBackground = true)
@Composable
private fun WeeklyForecastCardEqualTempsDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        WeeklyForecastCard(
            days = listOf(
                WeeklyForecastDay(dayLabel = "Mon", weatherCode = 0, precipitationChance = 0, lowTemperature = 20, highTemperature = 20),
                WeeklyForecastDay(dayLabel = "Tue", weatherCode = 0, precipitationChance = 0, lowTemperature = 20, highTemperature = 20),
                WeeklyForecastDay(dayLabel = "Wed", weatherCode = 0, precipitationChance = 0, lowTemperature = 20, highTemperature = 20),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light - Rainy week (high precipitation)", showBackground = true)
@Composable
private fun WeeklyForecastCardRainyWeekLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        WeeklyForecastCard(
            days = listOf(
                WeeklyForecastDay(dayLabel = "Mon", weatherCode = 61, precipitationChance = 80, lowTemperature = 12, highTemperature = 16),
                WeeklyForecastDay(dayLabel = "Tue", weatherCode = 63, precipitationChance = 90, lowTemperature = 11, highTemperature = 15),
                WeeklyForecastDay(dayLabel = "Wed", weatherCode = 95, precipitationChance = 100, lowTemperature = 10, highTemperature = 14),
                WeeklyForecastDay(dayLabel = "Thu", weatherCode = 80, precipitationChance = 70, lowTemperature = 11, highTemperature = 16),
                WeeklyForecastDay(dayLabel = "Fri", weatherCode = 51, precipitationChance = 40, lowTemperature = 12, highTemperature = 17),
                WeeklyForecastDay(dayLabel = "Sat", weatherCode = 3, precipitationChance = 15, lowTemperature = 13, highTemperature = 19),
                WeeklyForecastDay(dayLabel = "Sun", weatherCode = 2, precipitationChance = 4, lowTemperature = 14, highTemperature = 21),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Dark - Heatwave (dry week)", showBackground = true)
@Composable
private fun WeeklyForecastCardHeatwaveDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        WeeklyForecastCard(
            days = listOf(
                WeeklyForecastDay(dayLabel = "Mon", weatherCode = 0, precipitationChance = 0, lowTemperature = 24, highTemperature = 34),
                WeeklyForecastDay(dayLabel = "Tue", weatherCode = 0, precipitationChance = 0, lowTemperature = 25, highTemperature = 35),
                WeeklyForecastDay(dayLabel = "Wed", weatherCode = 1, precipitationChance = 0, lowTemperature = 26, highTemperature = 37),
                WeeklyForecastDay(dayLabel = "Thu", weatherCode = 0, precipitationChance = 0, lowTemperature = 25, highTemperature = 36),
                WeeklyForecastDay(dayLabel = "Fri", weatherCode = 0, precipitationChance = 0, lowTemperature = 24, highTemperature = 33),
                WeeklyForecastDay(dayLabel = "Sat", weatherCode = 2, precipitationChance = 4, lowTemperature = 23, highTemperature = 31),
                WeeklyForecastDay(dayLabel = "Sun", weatherCode = 1, precipitationChance = 0, lowTemperature = 22, highTemperature = 30),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
