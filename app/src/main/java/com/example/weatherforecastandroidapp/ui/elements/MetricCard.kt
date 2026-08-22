package com.example.weatherforecastandroidapp.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecastandroidapp.ui.theme.WeatherForeCastAndroidAppTheme
import java.util.Locale

/**
 * Reusable "Grid Card" metric tile (per DESIGN..txt: "Grid Cards: Square-ish aspect ratio for
 * metrics like UV Index and Visibility") — small icon + uppercase label header, a big value with
 * an optional unit, and a muted caption line underneath.
 *
 * Presentation-only: takes primitives, not a domain model, so callers (Humidity, Wind, UV Index,
 * Pressure, or anything else that fits this shape) format their own values. Callers arrange
 * several of these into a grid — see the 2x2 previews below for the reference layout.
 *
 * [icon] is an emoji glyph, matching the emoji-as-icon-stand-in convention already used by
 * [com.example.weatherforecastandroidapp.util.WeatherCodeMapper] — there's no vector icon set for
 * these metrics in the project (material-icons-extended isn't a dependency).
 */
@Composable
fun MetricCard(
    icon: String,
    label: String,
    value: String,
    caption: String,
    unit: String? = null,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(colorScheme.surfaceContainer)
            .padding(16.dp)
            // Merge the child text nodes into a single TalkBack announcement instead of four
            // separate ones (icon glyph is skipped since it carries no extra information here).
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = icon,
                fontSize = 14.sp,
                // Decorative: the label text next to it already conveys what this metric is.
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(
                text = label.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.onSurface,
            )
            if (unit != null) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Grid - Light", showBackground = true)
@Composable
private fun MetricCardGridLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        MetricCardGridPreviewContent()
    }
}

@Preview(name = "Grid - Dark", showBackground = true)
@Composable
private fun MetricCardGridDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        MetricCardGridPreviewContent()
    }
}

// Reference-layout preview: Humidity / Wind on top, UV Index / Pressure on bottom, matching the
// supplied light and dark reference designs exactly.
@Composable
private fun MetricCardGridPreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                icon = "💧",
                label = "Humidity",
                value = "65",
                unit = "%",
                caption = "Dew point 16°",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                icon = "💨",
                label = "Wind",
                value = "12",
                unit = "km/h",
                caption = "Direction: WNW",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                icon = "☀️",
                label = "UV Index",
                value = "4",
                caption = "Moderate",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                // No emoji reads as "atmospheric pressure" — cyclone is the closest common
                // convention weather widgets reach for; flagged to the user as a weak fallback.
                icon = "🌀",
                label = "Pressure",
                value = "1012",
                unit = "hPa",
                caption = "Steady",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(name = "Tile - Humidity", showBackground = true)
@Composable
private fun MetricCardHumidityPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        MetricCard(
            icon = "💧",
            label = "Humidity",
            value = "65",
            unit = "%",
            caption = "Dew point 16°",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Tile - Wind", showBackground = true)
@Composable
private fun MetricCardWindPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        MetricCard(
            icon = "💨",
            label = "Wind",
            value = "12",
            unit = "km/h",
            caption = "Direction: WNW",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Tile - UV Index", showBackground = true)
@Composable
private fun MetricCardUvIndexPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        MetricCard(
            icon = "☀️",
            label = "UV Index",
            value = "4",
            caption = "Moderate",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Tile - Pressure", showBackground = true)
@Composable
private fun MetricCardPressurePreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        MetricCard(
            icon = "🌀",
            label = "Pressure",
            value = "1012",
            unit = "hPa",
            caption = "Steady",
            modifier = Modifier.padding(16.dp),
        )
    }
}
