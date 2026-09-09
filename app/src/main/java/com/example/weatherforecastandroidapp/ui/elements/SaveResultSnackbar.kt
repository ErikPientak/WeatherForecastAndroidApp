package com.example.weatherforecastandroidapp.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.ui.theme.WeatherForeCastAndroidAppTheme

/**
 * Custom [SnackbarVisuals] for the save-confirmation snackbar, carrying [isNewSave] alongside the
 * message so [SaveResultSnackbar] can render a genuine save (place just bookmarked) distinctly
 * from a no-op ("already saved") result, matching the [R.drawable.bookmark]/
 * [R.drawable.filled_bookmark] toggle icon already used in the top bar for the same states.
 */
data class SaveResultSnackbarVisuals(
    override val message: String,
    val isNewSave: Boolean,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

/**
 * "Atmospheric Material" themed replacement for the default M3 [androidx.compose.material3.Snackbar]
 * — a pill (shapes.small, the spec's "small component" bucket) instead of a card-shaped surface,
 * floated with a shadow since (unlike this app's other cards) a snackbar sits above content rather
 * than within the layout flow. Tone communicates the two distinct results:
 * - new save: primaryContainer/onPrimaryContainer + filled bookmark icon (this app's existing
 *   "now bookmarked" iconography, reused rather than introducing a new asset).
 * - already saved: surfaceContainerHigh/onSurfaceVariant (muted — nothing changed) + the outline
 *   bookmark icon, mirroring the top bar toggle's un-saved state.
 *
 * Falls back to the "new save" (primary) styling if [data] wasn't shown with [SaveResultSnackbarVisuals]
 * so this remains a safe drop-in `snackbar` lambda for [androidx.compose.material3.SnackbarHost].
 */
@Composable
fun SaveResultSnackbar(data: SnackbarData) {
    val isNewSave = (data.visuals as? SaveResultSnackbarVisuals)?.isNewSave ?: true
    val colorScheme = MaterialTheme.colorScheme

    val containerColor = if (isNewSave) colorScheme.primaryContainer else colorScheme.surfaceContainerHigh
    val contentColor = if (isNewSave) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
    val icon = if (isNewSave) R.drawable.filled_bookmark else R.drawable.bookmark

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 6.dp, shape = MaterialTheme.shapes.small)
            .background(containerColor, MaterialTheme.shapes.small)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = contentColor,
        )
        Text(
            text = data.visuals.message,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}

@Preview(name = "New save - Light", showBackground = true)
@Composable
private fun SaveResultSnackbarNewSaveLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        SaveResultSnackbar(PreviewSnackbarData("Saved to Saved Places", isNewSave = true))
    }
}

@Preview(name = "New save - Dark", showBackground = true)
@Composable
private fun SaveResultSnackbarNewSaveDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        SaveResultSnackbar(PreviewSnackbarData("Saved to Saved Places", isNewSave = true))
    }
}

@Preview(name = "Already saved - Light", showBackground = true)
@Composable
private fun SaveResultSnackbarAlreadySavedLightPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = false) {
        SaveResultSnackbar(PreviewSnackbarData("Already saved", isNewSave = false))
    }
}

@Preview(name = "Already saved - Dark", showBackground = true)
@Composable
private fun SaveResultSnackbarAlreadySavedDarkPreview() {
    WeatherForeCastAndroidAppTheme(darkTheme = true) {
        SaveResultSnackbar(PreviewSnackbarData("Already saved", isNewSave = false))
    }
}

/** Minimal [SnackbarData] stub so previews can drive [SaveResultSnackbar] without a real host. */
private class PreviewSnackbarData(
    message: String,
    isNewSave: Boolean,
) : SnackbarData {
    override val visuals: SnackbarVisuals = SaveResultSnackbarVisuals(message, isNewSave)
    override fun performAction() {}
    override fun dismiss() {}
}
