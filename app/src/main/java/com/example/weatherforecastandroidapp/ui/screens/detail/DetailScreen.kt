package com.example.weatherforecastandroidapp.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherforecastandroidapp.data.model.SavedPlace
import com.example.weatherforecastandroidapp.ui.elements.BaseScreen
import com.example.weatherforecastandroidapp.ui.elements.LoadingScreen
import com.example.weatherforecastandroidapp.ui.elements.cards.HomeCard
import com.example.weatherforecastandroidapp.ui.elements.cards.HourlyForecastCard
import com.example.weatherforecastandroidapp.ui.elements.cards.MetricCard
import com.example.weatherforecastandroidapp.ui.elements.cards.PrecipitationChanceGraphCard
import com.example.weatherforecastandroidapp.ui.elements.cards.WeeklyForecastCard
import com.example.weatherforecastandroidapp.R.string as Strings
import com.example.weatherforecastandroidapp.util.PressureCategoryMapper
import com.example.weatherforecastandroidapp.util.UvIndexMapper
import com.example.weatherforecastandroidapp.util.WindDirectionMapper
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun DetailScreen(
    placeId: Long,
    placeName: String,
    placeLatitude: Double,
    placeLongitude: Double,
    onBack: () -> Unit = {},
) {
    val viewModel = hiltViewModel<DetailScreenViewModel>()
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(placeId, placeLatitude, placeLongitude) {
        viewModel.loadForecast(placeLatitude, placeLongitude, placeName)
    }

    DetailScreenContent(
        state = state.value,
        onBack = onBack,
        onDeleteClick = {
            viewModel.deletePlace(
                place = SavedPlace(
                    id = placeId,
                    name = placeName,
                    admin1 = null,
                    country = null,
                    latitude = placeLatitude,
                    longitude = placeLongitude,
                ),
                onDeleted = onBack,
            )
        },
    )
}

@Composable
fun DetailScreenContent(
    state: DetailScreenUiState,
    onBack: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val precipitationGraphOptions = listOf(
        stringResource(Strings.forecast_range_daily),
        stringResource(Strings.forecast_range_hourly),
    )
    val topBarText = (state as? DetailScreenUiState.Success)?.locationName?.ifBlank { null }
        ?: stringResource(Strings.detail_screen_title)

    BaseScreen(
        topBarText = topBarText,
        onBackClick = onBack,
        actions = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(Strings.detail_delete_content_description),
                )
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (state) {
                is DetailScreenUiState.Loading -> {
                    LoadingScreen()
                }

                is DetailScreenUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = stringResource(state.errorMessage))
                    }
                }

                is DetailScreenUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        HomeCard(
                            temperature = state.temperature,
                            weatherCode = state.weatherCode,
                            isDay = state.isDay,
                            highTemperature = state.highTemperature,
                            lowTemperature = state.lowTemperature,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        )

                        Spacer(Modifier.height(10.dp))
                        //--------------METRIC CARD-----------------
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
                                //--------------HUMIDITY-----------------
                                MetricCard(
                                    icon = "💧",
                                    label = stringResource(Strings.metric_humidity_label),
                                    value = state.humidity.toString(),
                                    unit = stringResource(Strings.unit_percent),
                                    caption = stringResource(
                                        Strings.metric_dew_point_caption,
                                        state.dewPoint
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                                //--------------WIND-----------------
                                MetricCard(
                                    icon = "💨",
                                    label = stringResource(Strings.metric_wind_label),
                                    value = state.windSpeed.toString(),
                                    unit = stringResource(Strings.unit_km_h),
                                    caption = stringResource(
                                        Strings.metric_wind_direction_caption,
                                        stringResource(WindDirectionMapper.compassRes(state.windDirection)),
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                //--------------UV INDEX-----------------
                                val currentUvIndex = state.hourlyForecast
                                    .getOrNull(LocalDateTime.now().hour)
                                    ?.uvIndex
                                    ?.roundToInt()
                                MetricCard(
                                    icon = "☀️",
                                    label = stringResource(Strings.metric_uv_index_label),
                                    value = currentUvIndex?.toString() ?: "–",
                                    caption = stringResource(
                                        currentUvIndex?.let { UvIndexMapper.categoryRes(it) }
                                            ?: Strings.weather_unknown
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                                //--------------PRESSURE-----------------
                                MetricCard(
                                    icon = "🌀",
                                    label = stringResource(Strings.metric_pressure_label),
                                    value = state.pressure.toString(),
                                    unit = stringResource(Strings.unit_hpa),
                                    caption = stringResource(
                                        PressureCategoryMapper.categoryRes(
                                            state.pressure
                                        )
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        //--------------HOURLY FORECAST-----------------
                        HourlyForecastCard(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            items = state.hourlyForecast,
                        )

                        Spacer(Modifier.height(20.dp))
                        //--------------PRECIPITATION CHANCE (DAILY/HOURLY TOGGLE)-----------------
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            precipitationGraphOptions.forEachIndexed { index, label ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = precipitationGraphOptions.size,
                                    ),
                                    onClick = { selectedIndex = index },
                                    selected = index == selectedIndex,
                                    label = { Text(label) },
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        PrecipitationChanceGraphCard(
                            points = if (selectedIndex == 0) state.dailyPrecipitation
                            else state.hourlyPrecipitation,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(Modifier.height(20.dp))
                        //--------------WEEKLY FORECAST-----------------
                        WeeklyForecastCard(
                            days = state.weeklyForecast,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
