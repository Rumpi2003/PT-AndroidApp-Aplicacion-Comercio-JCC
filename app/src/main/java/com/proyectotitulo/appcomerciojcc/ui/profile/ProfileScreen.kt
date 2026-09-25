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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proyectotitulo.appcomerciojcc.domain.models.GetCommuneResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetPrivateProfileResponse
import com.proyectotitulo.appcomerciojcc.domain.models.ProfileUiState

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel
) {

    val uiState by viewModel.uiState.observeAsState(initial = ProfileUiState.Loading)
    var showEditDescriptionDialog by remember { mutableStateOf(false) }
    var showEditCommuneDialog by remember { mutableStateOf(false) }
    var showEditGeoRadiusDialog by remember { mutableStateOf(false) }
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
                    Profile(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        vArrangement = Arrangement.spacedBy(12.dp),
                        hAlignment = Alignment.CenterHorizontally,
                        profile = profile,
                        onEditDescriptionClick = { showEditDescriptionDialog = true },
                        onEditCommuneClick = {
                            viewModel.loadCommunes()
                            showEditCommuneDialog = true
                        },
                        onEditGeoRadiusClick = { showEditGeoRadiusDialog = true }
                    )

                    if (showEditDescriptionDialog) {
                        EditDescriptionDialog(
                            initialDescription = profile.profileDescription.orEmpty(),
                            onDismissRequest = { showEditDescriptionDialog = false },
                            onConfirm = { newDesc, onComplete ->
                                viewModel.updateDescription(newDesc, onComplete)
                            }
                        )
                    }

                    if (showEditCommuneDialog && profile.commune != null) {
                        EditCommuneDialog(
                            currentCommune = profile.commune,
                            communesList = communesList,
                            onDismissRequest = { showEditCommuneDialog = false },
                            onConfirm = { selectedCommune, onComplete ->
                                viewModel.updateCommune(selectedCommune, onComplete)
                            }
                        )
                    }

                    if (showEditGeoRadiusDialog) {
                        EditGeoRadiusDialog(
                            currentGeoRadiusMeters = profile.geoRadius ?: 0,
                            onDismissRequest = { showEditGeoRadiusDialog = false },
                            onConfirm = { newRadiusMeters, onComplete ->
                                viewModel.updateGeoRadius(newRadiusMeters, onComplete)
                            }
                        )
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
    modifier: Modifier,
    vArrangement: Arrangement.Vertical,
    hAlignment: Alignment.Horizontal,
    profile: GetPrivateProfileResponse.Profile,
    onEditDescriptionClick: () -> Unit,
    onEditCommuneClick: () -> Unit,
    onEditGeoRadiusClick: () -> Unit
) {
    Column(
        modifier = modifier,
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
            value = "${profile.geoRadius ?: 0 / 1000} km",
            buttonText = "Cambiar",
            onEditClick = onEditGeoRadiusClick
        )
        VisibilityCard(
            isPublic = profile.profileVisibility ?: false
        )
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
    averageScore: Float,
) {

    val filledStars = averageScore.toInt().coerceIn(0, 5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column{
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
                        val icon = if (index < filledStars) Icons.Default.Star else Icons.Default.StarOutline
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
                        color = MaterialTheme.colorScheme.onBackground,
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
                label = "MIEMBRO DESDE",
                value = registerDate
            )
        }
    }
}

@Composable
fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
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
fun SingleActionCard(
    icon: ImageVector,
    label: String,
    value: String,
    buttonText: String,
    onEditClick: () -> Unit = {}
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
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
                Text(text = buttonText, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun VisibilityCard(isPublic: Boolean) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Visibilidad del perfil",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isPublic) "Público - visible para todos" else "Privado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Switch(
                checked = isPublic,
                onCheckedChange = {},
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
fun EditDescriptionDialog(
    initialDescription: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String, (Boolean, String?) -> Unit) -> Unit
) {
    var descriptionText by remember { mutableStateOf(initialDescription) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val maxChar = 255

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        title = {
            Text(
                text = "Editar descripción",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = {
                        if (it.length <= maxChar) {
                            descriptionText = it
                        }
                    },
                    label = { Text("Descripción del perfil") },
                    supportingText = {
                        Text(
                            text = "${descriptionText.length} / $maxChar",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Icono Descripción"
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
                    enabled = !isLoading
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    onConfirm(descriptionText) { success, error ->
                        isLoading = false
                        if (success) {
                            onDismissRequest()
                        } else {
                            errorMessage = error
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Aceptar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCommuneDialog(
    currentCommune: GetPrivateProfileResponse.Profile.Commune,
    communesList: List<GetCommuneResponse.Commune>,
    onDismissRequest: () -> Unit,
    onConfirm: (GetCommuneResponse.Commune, (Boolean, String?) -> Unit) -> Unit
) {
    var selectedCommune by remember {
        mutableStateOf<GetCommuneResponse.Commune?>(
            GetCommuneResponse.Commune(
                communeId = currentCommune.communeId,
                communeName = currentCommune.communeName,
                region = currentCommune.region
            )
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        title = {
            Text(
                text = "Cambiar comuna",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (!isLoading) expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCommune?.let { "${it.communeName} (${it.region})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Comuna") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.HomeWork,
                                contentDescription = "Icono Comuna"
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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
                        enabled = !isLoading
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        communesList.forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.communeName} (${item.region})") },
                                onClick = {
                                    selectedCommune = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCommune?.let { commune ->
                        isLoading = true
                        errorMessage = null
                        onConfirm(commune) { success, error ->
                            isLoading = false
                            if (success) {
                                onDismissRequest()
                            } else {
                                errorMessage = error
                            }
                        }
                    }
                },
                enabled = !isLoading && selectedCommune != null && selectedCommune?.communeId != currentCommune.communeId
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Aceptar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditGeoRadiusDialog(
    currentGeoRadiusMeters: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (Int, (Boolean, String?) -> Unit) -> Unit
) {
    var radiusMText by remember { mutableStateOf(currentGeoRadiusMeters.toString()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        title = {
            Text(
                text = "Cambiar radio de geolocalización",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = radiusMText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            radiusMText = input
                            errorMessage = null
                        }
                    },
                    label = { Text("Radio de búsqueda (metros)") },
                    supportingText = {
                        Text("Ingresa la distancia máxima en metros (m)")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Icono Radio"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = radiusMText.toIntOrNull()
                    if (m == null || m <= 200) {
                        errorMessage = "Ingresa un número mayor a 200 m"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    onConfirm(m) { success, error ->
                        isLoading = false
                        if (success) {
                            onDismissRequest()
                        } else {
                            errorMessage = error
                        }
                    }
                },
                enabled = !isLoading && radiusMText.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Aceptar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}
