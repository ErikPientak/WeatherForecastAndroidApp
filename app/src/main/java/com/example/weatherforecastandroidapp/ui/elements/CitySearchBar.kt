package com.example.weatherforecastandroidapp.ui.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult

// Docked (not full-screen) search takeover: results render as a bounded, elevated panel anchored
// below the input field, with the rest of the screen still visible behind it — the M3 `SearchBar`
// component with `expanded = true` is instead a full-screen scrim pattern, which isn't wanted here.
//
// Takes primitives rather than a screen-specific state type (originally ForecastSearchState)
// since this is shared between HomeScreen and ForecastScreen, each with their own distinct
// <Screen>SearchState data class — same decoupled-presentation convention as HourlyForecastItem/
// PrecipitationPoint elsewhere in ui/elements.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySearchBar(
    query: String,
    isSearching: Boolean,
    results: List<PlaceSearchResult>,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onPlaceSelected: (PlaceSearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = true,
                onExpandedChange = { if (!it) onDismiss() },
                placeholder = { Text(stringResource(R.string.forecast_search_placeholder)) },
                leadingIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.forecast_search_close_content_description),
                        )
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.forecast_search_clear_content_description),
                            )
                        }
                    }
                },
            )
        },
        expanded = true,
        onExpandedChange = { if (!it) onDismiss() },
        // Same surfaceContainer/shapes.medium chrome the rest of this app's cards use (see
        // MetricCard.kt, PrecipitationChanceGraphCard.kt) so the floating panel reads as
        // consistent with the app's visual language rather than M3's default docked styling.
        shape = MaterialTheme.shapes.medium,
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        when {
            isSearching -> {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            query.isNotBlank() && results.isEmpty() -> {
                Text(
                    text = stringResource(R.string.forecast_search_no_results),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                // Capped so a long result list scrolls internally instead of pushing the docked
                // panel to fill the screen (the full-screen-takeover behavior this fix removes).
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    itemsIndexed(results) { index, result ->
                        if (index > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        CitySearchResultRow(result = result, onClick = { onPlaceSelected(result) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CitySearchResultRow(result: PlaceSearchResult, onClick: () -> Unit) {
    val subtitle = listOfNotNull(result.admin1, result.country).joinToString(", ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            // Decorative: the name/subtitle text next to it already conveys this is a place.
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = result.name, style = MaterialTheme.typography.bodyLarge)
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
