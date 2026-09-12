package com.proyectotitulo.appcomerciojcc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.proyectotitulo.appcomerciojcc.ui.login.LoginScreen
import com.proyectotitulo.appcomerciojcc.ui.login.LoginViewModel
import com.proyectotitulo.appcomerciojcc.ui.theme.AppComercioJCCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppComercioJCCTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(LoginViewModel(),
                        modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
