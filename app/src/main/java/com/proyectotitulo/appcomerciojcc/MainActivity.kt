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
import androidx.navigation.toRoute
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
import com.proyectotitulo.appcomerciojcc.ui.home.HomeViewModel
import com.proyectotitulo.appcomerciojcc.ui.inventory.InventoryScreen
import com.proyectotitulo.appcomerciojcc.ui.inventory.InventoryViewModel
import com.proyectotitulo.appcomerciojcc.ui.navigation.HomeRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.InventoryRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.ProfileRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.PublicProfileRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.SearchRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.TradeRoute
import com.proyectotitulo.appcomerciojcc.ui.profile.ProfileScreen
import com.proyectotitulo.appcomerciojcc.ui.profile.ProfileViewModel
import com.proyectotitulo.appcomerciojcc.ui.profile.PublicProfileScreen
import com.proyectotitulo.appcomerciojcc.ui.profile.PublicProfileViewModel
import com.proyectotitulo.appcomerciojcc.ui.search.SearchScreen
import com.proyectotitulo.appcomerciojcc.ui.search.SearchViewModel
import com.proyectotitulo.appcomerciojcc.ui.trade.TradeScreen
import com.proyectotitulo.appcomerciojcc.ui.trade.TradeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KtorApiClient.setupCoil(this)
        val sessionManager = SessionManager(this)
        val initialToken = sessionManager.getToken()
        KtorApiClient.token = initialToken

        enableEdgeToEdge()
        setContent {
            AppComercioJCCTheme {
                val navController = rememberNavController()

                val homeViewModel: HomeViewModel = viewModel()
                val inventoryViewModel: InventoryViewModel = viewModel()
                val searchViewModel: SearchViewModel = viewModel()
                val profileViewModel: ProfileViewModel = viewModel()
                val tradeViewModel: TradeViewModel = viewModel()

                KtorApiClient.onUnauthorized = {
                    runOnUiThread {
                        homeViewModel.disableGeoModeOnLogout {
                            sessionManager.clearSession()
                            KtorApiClient.token = null

                            inventoryViewModel.clearData()
                            profileViewModel.clearData()
                            searchViewModel.clearData()
                            tradeViewModel.clearData()

                            navController.navigate(LoginRoute) {
                                popUpTo(0) { inclusive = true}
                            }
                        }
                    }
                }

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
                                    homeViewModel.clearData()
                                    inventoryViewModel.clearData()
                                    profileViewModel.clearData()
                                    searchViewModel.clearData()
                                    tradeViewModel.clearData()

                                    homeViewModel.loadInitialGeoMode()
                                    inventoryViewModel.loadInventory(forceRefresh = true)
                                    profileViewModel.loadProfile(forceRefresh = true)
                                    tradeViewModel.loadActiveTransactions(forceRefresh = true)

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
                            InventoryScreen(
                                viewModel = inventoryViewModel
                            )
                        }

                        composable<SearchRoute> {
                            SearchScreen(
                                viewModel = searchViewModel,
                                onCardAddedToInventory = {
                                    inventoryViewModel.loadInventory(forceRefresh = true)
                                },
                                onCardAddedToWishlist = {
                                    inventoryViewModel.loadWishlist(forceRefresh = true)
                                }
                            )
                        }

                        composable<HomeRoute> {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onUserClick = { userId ->
                                    navController.navigate(PublicProfileRoute(userId))
                                }
                            )
                        }

                        composable<TradeRoute> {
                            TradeScreen(
                                viewModel = tradeViewModel,
                                onUserClick = { userId ->
                                    navController.navigate(PublicProfileRoute(userId))
                                }
                            )
                        }

                        composable<ProfileRoute> {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                onLogout = {
                                    homeViewModel.disableGeoModeOnLogout {
                                        sessionManager.clearSession()
                                        KtorApiClient.token = null

                                        inventoryViewModel.clearData()
                                        profileViewModel.clearData()
                                        searchViewModel.clearData()
                                        tradeViewModel.clearData()

                                        navController.navigate(LoginRoute) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable<PublicProfileRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<PublicProfileRoute>()
                            val publicProfileViewModel: PublicProfileViewModel = viewModel()
                            PublicProfileScreen(
                                userId = route.userId,
                                viewModel = publicProfileViewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
