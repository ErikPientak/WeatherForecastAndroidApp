package com.example.weatherforecastandroidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.weatherforecastandroidapp.navigation.NavGraph
import com.example.weatherforecastandroidapp.ui.theme.WeatherForeCastAndroidAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherForeCastAndroidAppTheme {
                NavGraph()
            }
        }
    }
}