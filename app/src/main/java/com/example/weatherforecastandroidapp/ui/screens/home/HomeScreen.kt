package com.example.weatherforecastandroidapp.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.weatherforecastandroidapp.ui.elements.BaseScreen

@Composable
fun HomeScreen() {
    HomeScreenContent()
}

@Composable
fun HomeScreenContent(){
    BaseScreen(
        topBarText = "Home",
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            }
        }) {

    }
}
