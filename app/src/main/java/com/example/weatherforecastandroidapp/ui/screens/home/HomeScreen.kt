package com.example.weatherforecastandroidapp.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.ui.elements.BaseScreen
import com.example.weatherforecastandroidapp.ui.elements.LoadingScreen
import com.example.weatherforecastandroidapp.ui.elements.cards.HomeCard
import com.example.weatherforecastandroidapp.ui.elements.cards.HourlyForecastCard
import com.example.weatherforecastandroidapp.ui.elements.cards.MetricCard
import com.example.weatherforecastandroidapp.util.PressureCategoryMapper
import com.example.weatherforecastandroidapp.util.UvIndexMapper
import com.example.weatherforecastandroidapp.util.WindDirectionMapper
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun HomeScreen() {
    val viewModel = hiltViewModel<HomeScreenViewModel>()
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ granted ->
        viewModel.onAction(if (granted) HomeScreenActions.LocationPermissionGranted
        else HomeScreenActions.LocationPermissionDenied)
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            viewModel.onAction(HomeScreenActions.LocationPermissionGranted)
        }else{
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    HomeScreenContent(
        state = state.value,
        onAction = viewModel::onAction
    )
}

@Composable
fun HomeScreenContent(
    state: HomeScreenUiState,
    onAction: (HomeScreenActions) -> Unit,
){
    BaseScreen(
        topBarText = stringResource(R.string.nav_home),
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            }
        }
    ) { paddingValues ->
        when(state){
            is HomeScreenUiState.Loading -> {
                LoadingScreen()
            }
            is HomeScreenUiState.PermissionRequired -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = stringResource(R.string.home_permission_required))
                }
            }
            is HomeScreenUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = stringResource(state.errorMessage))
                }
            }
            is HomeScreenUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                ){
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
                                label = stringResource(R.string.metric_humidity_label),
                                value = state.humidity.toString(),
                                unit = stringResource(R.string.unit_percent),
                                caption = stringResource(R.string.metric_dew_point_caption, state.dewPoint),
                                modifier = Modifier.weight(1f),
                            )
                            //--------------WIND-----------------
                            MetricCard(
                                icon = "💨",
                                label = stringResource(R.string.metric_wind_label),
                                value = state.windSpeed.toString(),
                                unit = stringResource(R.string.unit_km_h),
                                caption = stringResource(
                                    R.string.metric_wind_direction_caption,
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
                            val currentUvIndex = state.hourlyForecast[LocalDateTime.now().hour].uvIndex
                                ?.roundToInt()
                            MetricCard(
                                icon = "☀️",
                                label = stringResource(R.string.metric_uv_index_label),
                                value = currentUvIndex?.toString() ?: "–",
                                caption = stringResource(
                                    currentUvIndex?.let { UvIndexMapper.categoryRes(it) }
                                        ?: R.string.weather_unknown
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            //--------------PRESSURE-----------------
                            MetricCard(
                                // No emoji reads as "atmospheric pressure" — cyclone is the closest common
                                // convention weather widgets reach for; flagged to the user as a weak fallback.
                                icon = "🌀",
                                label = stringResource(R.string.metric_pressure_label),
                                value = state.pressure.toString(),
                                unit = stringResource(R.string.unit_hpa),
                                caption = stringResource(PressureCategoryMapper.categoryRes(state.pressure)),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    //--------------HOURLY FORECAST-----------------
                    HourlyForecastCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        items = state.hourlyForecast
                    )
                }
            }
        }
    }
}
