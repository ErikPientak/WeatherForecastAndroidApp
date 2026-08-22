package com.example.weatherforecastandroidapp.ui.elements

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScreen(
    topBarText: String,
    onBackClick: (() -> Unit)? = null,
    showLoading: Boolean = false,
    floatingActionButton: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (paddingValues: PaddingValues) -> Unit
){
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = topBarText)
            }, navigationIcon = {
                if (onBackClick != null){
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            }, actions = actions)
        },
        floatingActionButton = floatingActionButton
    ) {
        if (showLoading) {
            LoadingScreen()
        } else {
            content(it)
        }
    }
}