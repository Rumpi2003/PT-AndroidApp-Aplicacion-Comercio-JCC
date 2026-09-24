package com.proyectotitulo.appcomerciojcc.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.proyectotitulo.appcomerciojcc.R
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import com.proyectotitulo.appcomerciojcc.data.local.SessionManager
import com.proyectotitulo.appcomerciojcc.domain.models.LoginUiState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier){
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Login(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            vArrangement = Arrangement.Center,
            hAlignment = Alignment.CenterHorizontally,
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess,
            onNavigateToRegister = onNavigateToRegister
        )
    }
}

@Composable
fun Login(
    modifier: Modifier,
    vArrangement: Arrangement.Vertical,
    hAlignment: Alignment.Horizontal,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit) {

    val email: String by viewModel.email.observeAsState(initial = "")
    val password: String by viewModel.password.observeAsState(initial = "")
    val loginEnable: Boolean by viewModel.loginEnable.observeAsState(initial = false)
    val uiState: LoginUiState by viewModel.uiState.observeAsState(initial = LoginUiState.Idle)
    val isLoading = uiState is LoginUiState.Loading
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier.imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = vArrangement,
            horizontalAlignment = hAlignment
        ) {
            Header()
            Spacer(modifier = Modifier.height(32.dp))
            EmailField(email, enabled = !isLoading) { viewModel.onTextFieldChanged(it, password) }
            Spacer(modifier = Modifier.height(16.dp))
            PasswordField(password, enabled = !isLoading) { viewModel.onTextFieldChanged(email, it) }
            ForgotPassword()
            Spacer(modifier = Modifier.height(16.dp))
            ErrorMessage(uiState)
            Spacer(modifier = Modifier.height(24.dp))
            LoginButton(loginEnable, isLoading) { viewModel.onLoginSelected(sessionManager) }
            Spacer(modifier = Modifier.height(16.dp))
            GoToRegisterButton(onNavigateToRegister)
        }
    }
}

@Composable
fun Header() {
    Image(
        painter = painterResource(R.drawable.logotipo_p_sf),
        contentDescription = "Logotipo de la app"
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Ingresa tus credenciales para continuar",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun EmailField(email: String, enabled: Boolean, onTextFieldChanged: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = { onTextFieldChanged(it) },
        label = { Text("Correo electrónico")},
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Icono Correo"
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
fun PasswordField(password: String,enabled: Boolean, onTextFieldChanged: (String) -> Unit) {

    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = { onTextFieldChanged(it) },
        label = { Text("Contraseña") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Icono contraseña"
            )
        },
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible}) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
fun ForgotPassword() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        TextButton(onClick = { /* Lógica de recuperación */ }) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ErrorMessage(uiState: LoginUiState) {
    if (uiState is LoginUiState.Error) {
        val errorState = uiState as LoginUiState.Error
        errorState.errors?.forEach { detail ->
            Text(
                text = "• $detail",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun LoginButton(loginEnable: Boolean, isloading: Boolean, onLoginSelected: () -> Unit) {
    Button(
        onClick = { onLoginSelected() },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = MaterialTheme.shapes.medium,
        enabled = loginEnable && !isloading
    ) {
        if (isloading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun GoToRegisterButton(onNavigateToRegister: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "¿No tienes una cuenta?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = { onNavigateToRegister() }) {
            Text(
                text = "Regístrate",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

