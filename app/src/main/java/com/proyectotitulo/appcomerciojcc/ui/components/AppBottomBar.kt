package com.proyectotitulo.appcomerciojcc.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.proyectotitulo.appcomerciojcc.ui.navigation.*

sealed class BottomNavItem<T : Any>(
    val route: T,
    val label: String,
    val icon: ImageVector
) {
    object Inventory : BottomNavItem<InventoryRoute>(InventoryRoute, "Inventario", Icons.Default.Style)
    object Search : BottomNavItem<SearchRoute>(SearchRoute, "Búsqueda", Icons.Default.Search)
    object Home : BottomNavItem<HomeRoute>(HomeRoute, "Inicio", Icons.Default.Home)
    object Trade : BottomNavItem<TradeRoute>(TradeRoute, "Transacciones", Icons.Default.SwapVert)
    object Profile : BottomNavItem<ProfileRoute>(ProfileRoute, "Perfil", Icons.Default.Person)
}

@Composable
fun AppBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Inventory,
        BottomNavItem.Search,
        BottomNavItem.Home,
        BottomNavItem.Trade,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.hasRoute(item.route::class)
            } == true

            NavigationBarItem(
                selected = isSelected,
                label = { },
                icon = { Icon(item.icon, contentDescription =  item.label) },
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

            )
        }
    }
}