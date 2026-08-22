package com.example.weatherforecastandroidapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

private data class BottomNavItem(
    val destination: Destination,
    val icon: ImageVector,
    val label: String,
)

private val bottomBarItems = listOf(
    BottomNavItem(Destination.HomeScreen, Icons.Default.Home, "Home"),
    BottomNavItem(Destination.ForecastScreen, Icons.Default.DateRange, "Forecast"),
    BottomNavItem(Destination.SavedPlacesScreen, Icons.Default.Place, "Saved Places"),
)

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Destination = Destination.HomeScreen,
    navigationRouter: INavigationRouter = remember { NavigationRouterImpl(navController) },
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = bottomBarItems.any { item ->
        backStackEntry?.destination?.hasRoute(item.destination::class) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarItems.forEach { item ->
                        NavigationBarItem(
                            selected = backStackEntry?.destination?.hasRoute(item.destination::class) == true,
                            onClick = {
                                navController.navigate(item.destination) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<Destination.HomeScreen> {}
            composable<Destination.ForecastScreen> {}
            composable<Destination.SavedPlacesScreen> {}
            composable<Destination.SearchPlaceScreen> {}
            composable<Destination.SettingsScreen> {}
            composable<Destination.DetailScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Destination.DetailScreen>()
            }
        }
    }
}
