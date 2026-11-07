package com.proyectotitulo.appcomerciojcc.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proyectotitulo.appcomerciojcc.domain.models.GetCommuneResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetPrivateProfileResponse
import com.proyectotitulo.appcomerciojcc.domain.models.ProfileUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit = {},
    onNavigateToReviews: (Int, String) -> Unit = { _, _ -> }
) {

    val uiState by viewModel.uiState.observeAsState(initial = ProfileUiState.Loading)
    val isRefreshing by viewModel.isRefreshing.observeAsState(initial = false)
    var showEditDescriptionDialog by remember { mutableStateOf(false) }
    var showEditCommuneDialog by remember { mutableStateOf(false) }
    var showEditGeoRadiusDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val communesList by viewModel.communesList.observeAsState(initial = emptyList())

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Success -> {
                val profile = state.profileData
                if (profile == null) {
                    ErrorContent(
                        errors = listOf("No se pudo obtener la información del usuario."),
                        onRetry = { viewModel.loadProfile() }
                    )
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.loadProfile(forceRefresh = true) },
                        modifier = modifier.fillMaxSize()
                    ) {
                        Profile(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            profile = profile,
                            vArrangement = Arrangement.spacedBy(16.dp),
                            hAlignment = Alignment.CenterHorizontally,
                            onEditDescriptionClick = { showEditDescriptionDialog = true },
                            onEditCommuneClick = {
                                viewModel.loadCommunes()
                                showEditCommuneDialog = true
                            },
                            onEditGeoRadiusClick = { showEditGeoRadiusDialog = true },
                            onVisibilityChange = { newVisibility ->
                                viewModel.updateProfileVisibility(newVisibility) { _, _ -> }
                            },
                            onLogoutClick = { showLogoutDialog = true },
                            onNavigateToReviews = onNavigateToReviews
                        )

                        if (showEditDescriptionDialog) {
                            EditDescriptionDialog(
                                initialDescription = profile.profileDescription.orEmpty(),
                                onDismissRequest = { showEditDescriptionDialog = false },
                                onConfirm = { newDescription, onResult ->
                                    viewModel.updateDescription(newDescription) { success, errorMsg ->
                                        onResult(success, errorMsg)
                                        if (success) showEditDescriptionDialog = false
                                    }
                                }
                            )
                        }

                        if (showEditCommuneDialog) {
                            EditCommuneDialog(
                                currentCommuneId = profile.commune?.communeId,
                                communesList = communesList,
                                onDismissRequest = { showEditCommuneDialog = false },
                                onConfirm = { selectedCommune, onResult ->
                                    if (selectedCommune != null) {
                                        viewModel.updateCommune(selectedCommune) { success, errorMsg ->
                                            onResult(success, errorMsg)
                                            if (success) showEditCommuneDialog = false
                                        }
                                    } else {
                                        onResult(false, "Selecciona una comuna válida")
                                    }
                                }
                            )
                        }

                        if (showEditGeoRadiusDialog) {
                            EditGeoRadiusDialog(
                                initialRadiusKm = (profile.geoRadius ?: 0) / 1000,
                                onDismissRequest = { showEditGeoRadiusDialog = false },
                                onConfirm = { newRadiusKm, onResult ->
                                    viewModel.updateGeoRadius(newRadiusKm * 1000) { success, errorMsg ->
                                        onResult(success, errorMsg)
                                        if (success) showEditGeoRadiusDialog = false
                                    }
                                }
                            )
                        }

                        if (showLogoutDialog) {
                            LogoutConfirmDialog(
                                onDismissRequest = { showLogoutDialog = false },
                                onConfirmLogout = {
                                    showLogoutDialog = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            }
            is ProfileUiState.Error -> {
                ErrorContent(
                    errors = state.errors,
                    onRetry = { viewModel.loadProfile() }
                )
            }
        }
    }
}

@Composable
fun Profile(
    modifier: Modifier = Modifier,
    profile: GetPrivateProfileResponse.Profile,
    vArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    hAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    onEditDescriptionClick: () -> Unit,
    onEditCommuneClick: () -> Unit,
    onEditGeoRadiusClick: () -> Unit,
    onVisibilityChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToReviews: (Int, String) -> Unit = { _, _ -> }
) {

    val locale = LocalConfiguration.current.locales[0]
    val km = (profile.geoRadius ?: 0) / 1000.0
    val formattedRadius = String.format(locale, "%.2f km", km)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = vArrangement,
        horizontalAlignment = hAlignment
    ) {
        HeaderProfileCard(
            username = profile.username ?: "Usuario",
            averageScore = profile.averageScore ?: 0.0f
        )
        DescriptionCard(
            description = profile.profileDescription.orEmpty(),
            onEditClick = onEditDescriptionClick
        )
        ContactInfoCard(
            email = profile.email ?: "Sin correo",
            contact = profile.contact.orEmpty(),
            registerDate = profile.formattedRegisterDate ?: "Desconocida"
        )
        SingleActionCard(
            icon = Icons.Default.HomeWork,
            label = "COMUNA",
            value = profile.commune?.communeName ?: "Sin comuna",
            buttonText = "Cambiar",
            onEditClick = onEditCommuneClick
        )
        SingleActionCard(
            icon = Icons.Default.Radar,
            label = "RADIO GEOLOCALIZACIÓN",
            value = formattedRadius,
            buttonText = "Cambiar",
            onEditClick = onEditGeoRadiusClick
        )
        VisibilityCard(
            isPublic = profile.profileVisibility ?: false,
            onVisibilityChange = onVisibilityChange
        )

        Button(
            onClick = {
                profile.userId?.let { uid ->
                    onNavigateToReviews(uid, profile.username.orEmpty())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver mis reseñas recibidas")
        }

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ErrorContent(
    errors: List<String>?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ocurrió un error al cargar el perfil",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        errors?.forEach { detail ->
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
fun HeaderProfileCard(
    username: String,
    averageScore: Float
) {

    val filledStars = averageScore.toInt().coerceIn(0, 5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "@${username.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        val icon =
                            if (index < filledStars) Icons.Default.Star else Icons.Default.StarOutline
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$averageScore",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun DescriptionCard(
    description: String,
    onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DESCRIPCIÓN",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Button(
                    onClick = onEditClick,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Editar", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description.ifEmpty { "Sin descripción" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun ContactInfoCard(email: String, contact: String, registerDate: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileDetailItem(
                icon = Icons.Default.Email,
                label = "CORREO",
                value = email
            )
            ProfileDetailItem(
                icon = Icons.Default.Phone,
                label = "CONTACTO",
                value = contact.ifEmpty { "Sin contacto" }
            )
            ProfileDetailItem(
                icon = Icons.Default.CalendarToday,
                label = "REGISTRADO DESDE EL",
                value = registerDate
            )
        }
    }
}

@Composable
fun SingleActionCard(
    icon: ImageVector,
    label: String,
    value: String,
    buttonText: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Button(
                onClick = onEditClick,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = buttonText, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun VisibilityCard(
    isPublic: Boolean,
    onVisibilityChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "PERFIL PÚBLICO",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (isPublic) "Visible para otros usuarios" else "Oculto para otros usuarios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Switch(
                checked = isPublic,
                onCheckedChange = onVisibilityChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

@Composable
fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun EditDescriptionDialog(
    initialDescription: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String, (Boolean, String?) -> Unit) -> Unit
) {
    var descriptionText by remember { mutableStateOf(initialDescription) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismissRequest()
        },
        title = { Text("Editar Descripción") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Biografía / Descripción") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    errorMessage = null
                    onConfirm(descriptionText) { success, msg ->
                        isSubmitting = false
                        if (!success) {
                            errorMessage = msg ?: "Error al actualizar descripción"
                        }
                    }
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isSubmitting
            ) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCommuneDialog(
    currentCommuneId: Int?,
    communesList: List<GetCommuneResponse.Commune>,
    onDismissRequest: () -> Unit,
    onConfirm: (GetCommuneResponse.Commune?, (Boolean, String?) -> Unit) -> Unit
) {
    var selectedCommune by remember {
        mutableStateOf(communesList.find { it.communeId == currentCommuneId })
    }
    var isExpanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismissRequest()
        },
        title = { Text("Seleccionar Comuna") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { if (!isSubmitting) isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCommune?.communeName ?: "Sin comuna asignada",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Comuna") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isSubmitting
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin comuna asignada") },
                            onClick = {
                                selectedCommune = null
                                isExpanded = false
                            }
                        )
                        communesList.forEach { commune ->
                            DropdownMenuItem(
                                text = { Text("${commune.communeName} (${commune.region})") },
                                onClick = {
                                    selectedCommune = commune
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    errorMessage = null
                    onConfirm(selectedCommune) { success, msg ->
                        isSubmitting = false
                        if (!success) {
                            errorMessage = msg ?: "Error al actualizar comuna"
                        }
                    }
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isSubmitting
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditGeoRadiusDialog(
    initialRadiusKm: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (Int, (Boolean, String?) -> Unit) -> Unit
) {
    var radiusText by remember { mutableStateOf(initialRadiusKm.toString()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismissRequest()
        },
        title = { Text("Editar Radio de Geolocalización") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = radiusText,
                    onValueChange = { radiusText = it },
                    label = { Text("Radio en Kilómetros (Km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val kmValue = radiusText.toIntOrNull()
                    if (kmValue == null || kmValue <= 0) {
                        errorMessage = "Ingresa un radio válido mayor a 0"
                        return@Button
                    }

                    isSubmitting = true
                    errorMessage = null
                    onConfirm(kmValue) { success, msg ->
                        isSubmitting = false
                        if (!success) {
                            errorMessage = msg ?: "Error al actualizar radio"
                        }
                    }
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isSubmitting
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun LogoutConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirmLogout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Cerrar Sesión") },
        text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
        confirmButton = {
            Button(
                onClick = onConfirmLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Sí, Cerrar Sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
