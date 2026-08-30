package com.example.weatherforecastandroidapp.ui.screens.forecast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.ui.elements.BaseScreen
import com.example.weatherforecastandroidapp.ui.elements.CitySearchBar
import com.example.weatherforecastandroidapp.ui.elements.LoadingScreen
import com.example.weatherforecastandroidapp.ui.elements.cards.PrecipitationChanceGraphCard
import com.example.weatherforecastandroidapp.ui.elements.cards.WeeklyForecastCard

@Composable
fun ForecastScreen(){
    val viewModel = hiltViewModel<ForecastViewModel>()
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val searchState = viewModel.searchState.collectAsStateWithLifecycle()

    ForecastScreenContent(
        state = state.value,
        searchState = searchState.value,
        onAction = viewModel::onAction,
    )
}

@Composable
fun ForecastScreenContent(
    state: ForecastUiState,
    searchState: ForecastSearchState,
    onAction: (ForecastScreenActions) -> Unit,
){
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val precipitationGraphOptions = listOf(
        stringResource(R.string.forecast_range_daily),
        stringResource(R.string.forecast_range_hourly),
    )

    val locationName = (state as? ForecastUiState.Success)?.locationName?.ifBlank { null }
        ?: stringResource(R.string.nav_forecast)

    BaseScreen(
        topBarText = locationName,
        actions = {
            IconButton(onClick = { onAction(ForecastScreenActions.PlaceSaved) }) {
                Icon(
                    painter = painterResource(R.drawable.bookmark),
                    contentDescription = stringResource(R.string.forecast_save_content_description),
                )
            }

            IconButton(onClick = { onAction(ForecastScreenActions.SearchActivated) }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.forecast_search_content_description),
                )
            }
        }
    ){ paddingValues -> Box(modifier = Modifier.fillMaxSize()) {
        when(state){
            is ForecastUiState.Loading -> {
                LoadingScreen()
            }
            is ForecastUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = stringResource(state.errorMessage))
                }
            }
            is ForecastUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ){
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .align(Alignment.CenterHorizontally),
                    ){
                        precipitationGraphOptions.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = precipitationGraphOptions.size
                                ),
                                onClick = { selectedIndex = index },
                                selected = index == selectedIndex,
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    PrecipitationChanceGraphCard(
                        points = if (selectedIndex == 0) state.dailyForecast
                        else state.hourlyForecast,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(20.dp))

                    WeeklyForecastCard(
                        days = state.weeklyForecast,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        if (searchState.isActive) {
            CitySearchBar(
                query = searchState.query,
                isSearching = searchState.isSearching,
                results = searchState.results,
                onQueryChange = { onAction(ForecastScreenActions.SearchQueryChanged(it)) },
                onDismiss = { onAction(ForecastScreenActions.SearchDismissed) },
                onPlaceSelected = { onAction(ForecastScreenActions.PlaceSelected(it)) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(paddingValues)
                    .fillMaxWidth(),
            )
            }
        }
    }
}

