package com.example.weatherforecastandroidapp.ui.screens.savedPlaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.ui.elements.BaseScreen
import com.example.weatherforecastandroidapp.ui.elements.LoadingScreen
import com.example.weatherforecastandroidapp.ui.elements.cards.SavedLocationCard
import com.example.weatherforecastandroidapp.ui.elements.cards.SavedLocationWeather

@Composable
fun SavedPlacesScreen(
    onPlaceClick: (SavedLocationWeather) -> Unit = {},
) {
    val viewModel = hiltViewModel<SavedPlaceScreenViewModel>()
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    SavedPlaceScreenContent(
        state = state.value,
        onPlaceClick = onPlaceClick,
        onPlaceDelete = viewModel::removePlace,
    )
}

@Composable
fun SavedPlaceScreenContent(
    state: SavedPlacesScreenUiState,
    onPlaceClick: (SavedLocationWeather) -> Unit = {},
    onPlaceDelete: (SavedLocationWeather) -> Unit = {},
){
    var placeToDelete by remember { mutableStateOf<SavedLocationWeather?>(null) }

    BaseScreen(
        topBarText = stringResource(R.string.nav_saved_places),
    ) { paddingValues ->
        when(state){
            is SavedPlacesScreenUiState.Loading -> {
                LoadingScreen()
            }
            is SavedPlacesScreenUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = stringResource(state.errorMessage))
                }
            }
            is SavedPlacesScreenUiState.Success -> {
                SavedPlaceGrid(
                    places = state.places,
                    onPlaceClick = onPlaceClick,
                    onPlaceLongClick = { placeToDelete = it },
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }

    placeToDelete?.let { place ->
        AlertDialog(
            onDismissRequest = { placeToDelete = null },
            title = { Text(stringResource(R.string.delete_place_dialog_title)) },
            text = { Text(stringResource(R.string.delete_place_dialog_message, place.cityName)) },
            confirmButton = {
                TextButton(onClick = {
                    onPlaceDelete(place)
                    placeToDelete = null
                }) {
                    Text(stringResource(R.string.delete_place_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { placeToDelete = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}

@Composable
fun SavedPlaceGrid(
    places: List<SavedLocationWeather>,
    onPlaceClick: (SavedLocationWeather) -> Unit = {},
    onPlaceLongClick: (SavedLocationWeather) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(places.size) { index ->
            val weather = places[index]
            SavedLocationCard(
                weather = weather,
                onClick = { onPlaceClick(weather) },
                onLongClick = { onPlaceLongClick(weather) },
            )
        }
    }
}