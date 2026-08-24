package com.example.weatherforecastandroidapp.ui.elements.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.ui.theme.WeatherForeCastAndroidAppTheme

/**
 * Presentation-only shape for a single point on the precipitation chance graph: a time-or-day
 * label and a 0-100 percentage. Same list-of-primitives, no-domain-model-coupling pattern as
 * [HourlyForecastItem] — the Hourly and Daily styles are the same card, just fed different
 * labels/lengths (e.g. "Now"/"12 PM"/... vs "Mon"/"Tue"/...).
 */
data class PrecipitationPoint(
    val label: String,
    val percentage: Int,
)

private val GridlinePercentages = listOf(0, 40, 80)
private const val GraphHeightDp = 140

/**
 * "Precipitation Chance" card: a title row above a hand-drawn smooth line + gradient-fill chart
 * (no charting library dependency — plain Canvas/Path), with per-point time/day + percentage
 * labels below the curve. The curve uses the theme's Primary ("Sky Blue") color family; the
 * spec's "Rain Indigo" is prose-only and was never wired up as an actual color token (tertiary is
 * already "Sunlit Gold" per DESIGN..txt), so Primary is the closest real match.
 */
@Composable
fun PrecipitationChanceGraphCard(
    points: List<PrecipitationPoint>,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "💧",
                fontSize = 20.sp,
                // Decorative: the title text right next to it already conveys what this card is.
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(
                text = stringResource(R.string.precipitation_chance_title),
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
            )
        }

        PrecipitationCurve(points = points, lineColor = colorScheme.primary, gridlineColor = colorScheme.outlineVariant)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            points.forEach { point ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${point.percentage}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PrecipitationCurve(
    points: List<PrecipitationPoint>,
    lineColor: Color,
    gridlineColor: Color,
) {
    val fillBrush = Brush.verticalGradient(
        listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0f))
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.height(GraphHeightDp.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            GridlinePercentages.reversed().forEach { percentage ->
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelSmall,
                    color = gridlineColor,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(GraphHeightDp.dp)
                // Decorative: the label/percentage row below spells out the same data as text.
                .clearAndSetSemantics {},
        ) {
            if (points.size < 2) return@Canvas

            val stepX = size.width / (points.size - 1)
            val offsets = points.mapIndexed { index, point ->
                val x = index * stepX
                val y = size.height * (1f - point.percentage.coerceIn(0, 100) / 100f)
                Offset(x, y)
            }

            GridlinePercentages.forEach { percentage ->
                val y = size.height * (1f - percentage / 100f)
                drawLine(
                    color = gridlineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            // Smooth curve: cubic bezier between each pair of points, control points placed
            // horizontally at the pair's midpoint so the curve eases in/out of each data point.
            val linePath = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                for (i in 0 until offsets.size - 1) {
                    val current = offsets[i]
                    val next = offsets[i + 1]
                    val midX = (current.x + next.x) / 2f
                    cubicTo(midX, current.y, midX, next.y, next.x, next.y)
                }
            }

            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(offsets.last().x, size.height)
                lineTo(offsets.first().x, size.height)
                close()
            }

            drawPath(path = fillPath, brush = fillBrush)
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

private val HourlySamplePoints = listOf(
    PrecipitationPoint("Now", 10),
    PrecipitationPoint("12 PM", 30),
    PrecipitationPoint("4 PM", 80),
    PrecipitationPoint("8 PM", 60),
    PrecipitationPoint("12 AM", 20),
)

private val DailySamplePoints = listOf(
    PrecipitationPoint("Mon", 10),
    PrecipitationPoint("Tue", 20),
    PrecipitationPoint("Wed", 65),
    PrecipitationPoint("Thu", 80),
    PrecipitationPoint("Fri", 40),
    PrecipitationPoint("Sat", 15),
    PrecipitationPoint("Sun", 5),
)

@Preview(name = "Hourly - Light", showBackground = true)
@Composable
private fun PrecipitationChanceGraphCardHourlyLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        PrecipitationChanceGraphCard(points = HourlySamplePoints, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Hourly - Dark", showBackground = true)
@Composable
private fun PrecipitationChanceGraphCardHourlyDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        PrecipitationChanceGraphCard(points = HourlySamplePoints, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Daily - Light", showBackground = true)
@Composable
private fun PrecipitationChanceGraphCardDailyLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        PrecipitationChanceGraphCard(points = DailySamplePoints, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Daily - Dark", showBackground = true)
@Composable
private fun PrecipitationChanceGraphCardDailyDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        PrecipitationChanceGraphCard(points = DailySamplePoints, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Hourly - Rainy day (high values)", showBackground = true)
@Composable
private fun PrecipitationChanceGraphCardRainyDayPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        PrecipitationChanceGraphCard(
            points = listOf(
                PrecipitationPoint("Now", 70),
                PrecipitationPoint("12 PM", 90),
                PrecipitationPoint("4 PM", 100),
                PrecipitationPoint("8 PM", 95),
                PrecipitationPoint("12 AM", 75),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Daily - Dry week (near zero)", showBackground = true)
@Composable
private fun PrecipitationChanceGraphCardDryWeekPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        PrecipitationChanceGraphCard(
            points = listOf(
                PrecipitationPoint("Mon", 0),
                PrecipitationPoint("Tue", 2),
                PrecipitationPoint("Wed", 0),
                PrecipitationPoint("Thu", 5),
                PrecipitationPoint("Fri", 0),
                PrecipitationPoint("Sat", 0),
                PrecipitationPoint("Sun", 3),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
