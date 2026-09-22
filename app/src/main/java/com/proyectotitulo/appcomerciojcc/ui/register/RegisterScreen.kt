package com.proyectotitulo.appcomerciojcc.ui.register

import android.os.Message
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proyectotitulo.appcomerciojcc.domain.models.CommuneResponse.Commune
import com.proyectotitulo.appcomerciojcc.domain.models.RegisterUiState

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Register(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            vArrangement = Arrangement.Center,
            hAlignment = Alignment.CenterHorizontally,
            viewModel = viewModel,
            onBackToLogin = onBackToLogin
        )
    }

}

@Composable
fun Register(
    modifier: Modifier,
    vArrangement: Arrangement.Vertical,
    hAlignment: Alignment.Horizontal,
    viewModel: RegisterViewModel,
    onBackToLogin: () -> Unit
) {

    val email: String by viewModel.email.observeAsState(initial = "")
    val username: String by viewModel.username.observeAsState(initial = "")
    val password: String by viewModel.password.observeAsState(initial = "")
    val confirmPassword by viewModel.confirmPassword.observeAsState(initial = "")
    val contact by viewModel.contact.observeAsState(initial = "")
    val communeId by viewModel.communeId.observeAsState(initial = 0)
    val communesList: List<Commune> by viewModel.communesList.observeAsState(initial = emptyList())
    val selectedCommuneName: String by viewModel.selectedCommuneName.observeAsState(initial = "")
    val profileDescription by viewModel.profileDescription.observeAsState(initial = "")
    val registerEnable by viewModel.registerEnable.observeAsState(initial = false)
    val uiState by viewModel.uiState.observeAsState(initial = RegisterUiState.Idle)
    val isLoading = uiState is RegisterUiState.Loading

    if (uiState is RegisterUiState.Success) {
        val successState = uiState as RegisterUiState.Success
        SuccessContent(
            message = successState.message,
            onBackToLogin = onBackToLogin
        )
    } else {

        val scrollState = rememberScrollState()

        Column(
            modifier = modifier.verticalScroll(scrollState),
            verticalArrangement = vArrangement,
            horizontalAlignment = hAlignment
        ) {
            Header()
            Spacer(modifier = Modifier.height(24.dp))
            EmailField(email, !isLoading) {
                viewModel.onFieldChanged(
                    it, username, password, confirmPassword,
                    contact, communeId, profileDescription
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            UsernameField(username, !isLoading) {
                viewModel.onFieldChanged(
                    email, it, password, confirmPassword,
                    contact, communeId, profileDescription
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            PasswordField(password, !isLoading) {
                viewModel.onFieldChanged(
                    email, username, it, confirmPassword,
                    contact, communeId, profileDescription
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ConfirmPasswordField(confirmPassword, !isLoading) {
                viewModel.onFieldChanged(
                    email, username, password, it,
                    contact, communeId, profileDescription
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            PhoneNumberField(contact, !isLoading) {
                viewModel.onFieldChanged(
                    email, username, password, confirmPassword,
                    it, communeId, profileDescription
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            CommuneField(
                selectedCommuneName = selectedCommuneName,
                communesList = communesList,
                enabled = !isLoading,
                onCommuneSelected = { viewModel.onCommuneSelected(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDescriptionField(profileDescription, !isLoading) {
                viewModel.onFieldChanged(
                    email, username, password, confirmPassword,
                    contact, communeId, it
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ErrorMessage(uiState)
            Spacer(modifier = Modifier.height(24.dp))
            RegisterButton(
                registerEnable = registerEnable,
                isLoading = isLoading,
                onRegisterSelected = { viewModel.onRegisterSelected() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            BackToLoginButton(onBackToLogin)
        }
    }
}

@Composable
fun Header() {
    Text(
        text = "Crear Cuenta",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Únete a la comunidad de comercio de cartas",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun EmailField(email: String, enabled: Boolean, onTextFieldChanged: (String) -> Unit) {

    val maxChar = 255

    OutlinedTextField(
        value = email,
        onValueChange = { if (it.length <= maxChar) onTextFieldChanged(it) },
        label = { Text("Correo electrónico*")},
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
fun UsernameField(username: String, enabled: Boolean, onTextFieldChanged: (String) -> Unit) {

    val maxChar = 32

    OutlinedTextField(
        value = username,
        onValueChange = { if (it.length <= maxChar) onTextFieldChanged(it) },
        label = { Text("Nombre de usuario*")},
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Icono Nombre Usuario"
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
fun PasswordField(password: String, enabled: Boolean, onTextFieldChanged: (String) -> Unit) {

    var isPasswordVisible by remember { mutableStateOf(false) }
    val maxChar = 64

    OutlinedTextField(
        value = password,
        onValueChange = { if (it.length <= maxChar) onTextFieldChanged(it) },
        label = { Text("Contraseña*") },
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Contraseña debe tener como mínimo:\n" +
                "   -8 carácteres\n" +
                "   -una letra minúscula y mayúscula\n" +
                "   -un número\n" +
                "   -un carácter especial",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Left,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)

    )
}

@Composable
fun ConfirmPasswordField(confirmPassword: String, enabled: Boolean, onTextFieldChanged: (String) -> Unit) {

    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    val maxChar = 64

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = { if (it.length <= maxChar) onTextFieldChanged(it) },
        label = { Text("Confirmar Contraseña*") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Icono contraseña"
            )
        },
        trailingIcon = {
            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible}) {
                Icon(
                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isConfirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
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
        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
fun PhoneNumberField(contact: String, enabled: Boolean, onTextFieldChanged: (String) -> Unit) {

    val maxChar = 12

    OutlinedTextField(
        value = contact,
        onValueChange = { if (it.length <= maxChar) onTextFieldChanged(it) },
        label = { Text("Teléfono Contacto") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Icono Teléfono"
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommuneField(
    selectedCommuneName: String,
    communesList: List<Commune>,
    enabled: Boolean,
    onCommuneSelected: (Commune) -> Unit
) {
    var expandedCommune by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandedCommune,
        onExpandedChange = { expandedCommune = it},
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCommuneName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Comuna*") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Icono Comuna"
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCommune)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expandedCommune,
            onDismissRequest = { expandedCommune = false }
        ) {
            communesList.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = "${item.communeName} (${item.region})") },
                    onClick = {
                        onCommuneSelected(item)
                        expandedCommune = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileDescriptionField(profileDescription: String, enabled: Boolean, onTextFieldChanged: (String) -> Unit) {

    val maxChar = 255

    OutlinedTextField(
        value = profileDescription,
        onValueChange = { if (it.length <= maxChar) onTextFieldChanged(it) },
        label = { Text("Descripción del perfil")},
        supportingText = {
            Text(
                text = "${profileDescription.length} / $maxChar",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Icono Descripción del Perfil"
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        minLines = 3,
        maxLines = 5,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        isError = profileDescription.length >= maxChar
    )
}

@Composable
fun ErrorMessage(uiState: RegisterUiState) {
    if (uiState is RegisterUiState.Error) {
        val errorState = uiState as RegisterUiState.Error
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
fun RegisterButton(
    registerEnable: Boolean,
    isLoading: Boolean,
    onRegisterSelected: () -> Unit
) {
    Button(
        onClick = { onRegisterSelected() },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = MaterialTheme.shapes.medium,
        enabled = registerEnable && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Registrarse",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun BackToLoginButton(onBackToLogin: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "¿Ya tienes una cuenta?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = { onBackToLogin() }) {
            Text(
                text = "Inicia sesión",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SuccessContent(
    message: String,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onBackToLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Volver al Inicio de Sesión")
        }
    }

}