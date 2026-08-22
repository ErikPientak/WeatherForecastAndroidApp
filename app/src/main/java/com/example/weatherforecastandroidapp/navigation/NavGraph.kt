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
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.weatherforecastandroidapp.ui.screens.forecast.ForecastScreen
import com.example.weatherforecastandroidapp.ui.screens.home.HomeScreen
import com.example.weatherforecastandroidapp.ui.screens.savedPlaces.SavedPlacesScreen
import com.example.weatherforecastandroidapp.R

private data class BottomNavItem(
    val destination: Destination,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
)

private val bottomBarItems = listOf(
    BottomNavItem(Destination.HomeScreen, Icons.Default.Home, R.string.nav_home),
    BottomNavItem(Destination.ForecastScreen, Icons.Default.DateRange, R.string.nav_forecast),
    BottomNavItem(Destination.SavedPlacesScreen, Icons.Default.Place, R.string.nav_saved_places),
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
                        val label = stringResource(item.labelRes)
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
                            icon = { Icon(item.icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            // Only clear the bottom nav bar here — no topBar on this Scaffold, so the top inset
            // is left for each screen's own BaseScreen/TopAppBar to consume exactly once itself.
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable<Destination.HomeScreen> { HomeScreen() }
            composable<Destination.ForecastScreen> { ForecastScreen() }
            composable<Destination.SavedPlacesScreen> { SavedPlacesScreen() }
            composable<Destination.SearchPlaceScreen> {}
            composable<Destination.SettingsScreen> {}
            composable<Destination.DetailScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Destination.DetailScreen>()
            }
        }
    }
}
