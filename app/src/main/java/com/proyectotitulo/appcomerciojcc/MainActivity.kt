package com.proyectotitulo.appcomerciojcc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.proyectotitulo.appcomerciojcc.ui.login.LoginScreen
import com.proyectotitulo.appcomerciojcc.ui.login.LoginViewModel
import com.proyectotitulo.appcomerciojcc.ui.navigation.LoginRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.RegisterRoute
import com.proyectotitulo.appcomerciojcc.ui.register.RegisterScreen
import com.proyectotitulo.appcomerciojcc.ui.register.RegisterViewModel
import com.proyectotitulo.appcomerciojcc.ui.theme.AppComercioJCCTheme
import androidx.navigation.NavDestination.Companion.hasRoute
import com.proyectotitulo.appcomerciojcc.data.local.SessionManager
import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.ui.components.AppBottomBar
import com.proyectotitulo.appcomerciojcc.ui.home.HomeScreen
import com.proyectotitulo.appcomerciojcc.ui.inventory.InventoryScreen
import com.proyectotitulo.appcomerciojcc.ui.navigation.HomeRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.InventoryRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.ProfileRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.SearchRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.TradeRoute
import com.proyectotitulo.appcomerciojcc.ui.profile.ProfileScreen
import com.proyectotitulo.appcomerciojcc.ui.profile.ProfileViewModel
import com.proyectotitulo.appcomerciojcc.ui.search.SearchScreen
import com.proyectotitulo.appcomerciojcc.ui.trade.TradeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        val initialToken = sessionManager.getToken()
        KtorApiClient.token = initialToken

        enableEdgeToEdge()
        setContent {
            AppComercioJCCTheme {
                val navController = rememberNavController()
                val startDest = if (initialToken != null) HomeRoute else LoginRoute
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val showBottomBar = currentDestination?.let { dest ->
                    !dest.hasRoute(LoginRoute::class) &&
                            !dest.hasRoute(RegisterRoute::class)
                } ?: false

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            AppBottomBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDest,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<LoginRoute> {
                            val loginViewModel: LoginViewModel = viewModel()
                            LoginScreen(
                                viewModel = loginViewModel,
                                onNavigateToRegister = {
                                    navController.navigate(RegisterRoute)
                                },
                                onLoginSuccess = {
                                    navController.navigate(HomeRoute) {
                                        popUpTo(LoginRoute) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<RegisterRoute> {
                            val registerViewModel: RegisterViewModel = viewModel()
                            RegisterScreen(
                                viewModel = registerViewModel,
                                onBackToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable<InventoryRoute> {
                            InventoryScreen()
                        }

                        composable<SearchRoute> {
                            SearchScreen()
                        }

                        composable<HomeRoute> {
                            HomeScreen()
                        }

                        composable<TradeRoute> {
                            TradeScreen()
                        }

                        composable<ProfileRoute> {
                            val profileViewModel: ProfileViewModel = viewModel()
                            ProfileScreen(
                                viewModel = profileViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
