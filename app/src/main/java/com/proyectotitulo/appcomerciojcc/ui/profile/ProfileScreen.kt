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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proyectotitulo.appcomerciojcc.domain.models.GetPrivateProfileResponse
import com.proyectotitulo.appcomerciojcc.domain.models.ProfileUiState

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel
) {

    val uiState by viewModel.uiState.observeAsState(initial = ProfileUiState.Loading)
    var showEditDialog by remember { mutableStateOf(false) }

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
                Profile(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    vArrangement = Arrangement.spacedBy(12.dp),
                    hAlignment = Alignment.CenterHorizontally,
                    profile = state.profileData,
                    onEditDescriptionClick = { showEditDialog = true }
                )

                if (showEditDialog) {
                    EditDescriptionDialog(
                        initialDescription = state.profileData.profileDescription,
                        onDismissRequest = { showEditDialog = false },
                        onConfirm = { newDesc, onComplete ->
                            viewModel.updateDescription(newDesc, onComplete)
                        }
                    )
                }
            }
            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    state.errors?.forEach { detail ->
                        Text(
                            text = "• $detail",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
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
    onEditDescriptionClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = vArrangement,
        horizontalAlignment = hAlignment
    ) {
        HeaderProfileCard(
            username = profile.username,
            averageScore = profile.averageScore
        )
        DescriptionCard(
            description = profile.profileDescription,
            onEditClick = onEditDescriptionClick
        )
        ContactInfoCard(
            email = profile.email,
            contact = profile.contact,
            registerDate = profile.formattedRegisterDate
        )
        SingleActionCard(
            icon = Icons.Default.HomeWork,
            label = "COMUNA",
            value = profile.commune.communeName,
            buttonText = "Cambiar"
        )
        SingleActionCard(
            icon = Icons.Default.Radar,
            label = "RADIO GEOLOCALIZACIÓN",
            value = "${profile.geoRadius / 1000} km",
            buttonText = "Cambiar"
        )
        VisibilityCard(
            isPublic = profile.profileVisibility
        )
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
    buttonText: String
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
                onClick = {},
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