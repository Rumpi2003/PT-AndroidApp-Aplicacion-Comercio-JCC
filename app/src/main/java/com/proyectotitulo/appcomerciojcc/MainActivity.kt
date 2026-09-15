package com.proyectotitulo.appcomerciojcc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proyectotitulo.appcomerciojcc.ui.login.LoginScreen
import com.proyectotitulo.appcomerciojcc.ui.login.LoginViewModel
import com.proyectotitulo.appcomerciojcc.ui.navigation.LoginRoute
import com.proyectotitulo.appcomerciojcc.ui.navigation.RegisterRoute
import com.proyectotitulo.appcomerciojcc.ui.register.RegisterScreen
import com.proyectotitulo.appcomerciojcc.ui.register.RegisterViewModel
import com.proyectotitulo.appcomerciojcc.ui.theme.AppComercioJCCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppComercioJCCTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = LoginRoute,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<LoginRoute> {
                            val loginViewModel: LoginViewModel = viewModel()
                            LoginScreen(
                                viewModel = loginViewModel,
                                onNavigateToRegister = {
                                    navController.navigate(RegisterRoute)
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
                    }
                }
            }
        }
    }
}
