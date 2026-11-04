package com.proyectotitulo.appcomerciojcc.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.proyectotitulo.appcomerciojcc.domain.models.GetMatchmakingResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTradeInventoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.HomeUiState
import com.proyectotitulo.appcomerciojcc.domain.models.TransactionState
import com.proyectotitulo.appcomerciojcc.ui.components.CardImageThumbnail
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.observeAsState(initial = HomeUiState())
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val requestLocationAndSend: ((Float, Float) -> Unit) -> Unit = { onLocationObtained ->
        fetchLocation(context, fusedLocationClient, onLocationObtained)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            requestLocationAndSend { lat, lng ->
                viewModel.sendLocationAndUpdateMatchmaking(lat, lng)
            }
        }
    }

    LaunchedEffect(uiState.useGeolocation) {
        if (uiState.useGeolocation && hasLocationPermission(context)) {
            requestLocationAndSend { lat, lng ->
                viewModel.sendLocationAndUpdateMatchmaking(lat, lng)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadRecommendations(forceRefresh = true) },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                HomeHeader(
                    isAllOffersMode = uiState.isAllOffersMode,
                    totalOffers = uiState.totalRecommendations,
                    onBackClick = { viewModel.exitAllOffersMode() }
                )

                GeoModeSwitchCard(
                    useGeolocation = uiState.useGeolocation,
                    useUserCommune = uiState.useUserCommune,
                    isLoadingGeoMode = uiState.isLoadingGeoMode,
                    onGeoModeToggle = { newGeoMode ->
                        if (newGeoMode) {
                            if (hasLocationPermission(context)) {
                                viewModel.toggleGeoMode(true, requestLocationAndSend)
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        } else {
                            viewModel.toggleGeoMode(false, requestLocationAndSend)
                        }
                    },
                    onUserCommuneToggle = { enabled ->
                        viewModel.toggleUserCommune(enabled)
                    }
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.errors != null) {
                    ErrorContent(
                        errors = uiState.errors,
                        onRetry = { viewModel.loadRecommendations(forceRefresh = true) }
                    )
                } else if (uiState.recommendations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No se encontraron ofertas de intercambio disponibles.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadRecommendations(forceRefresh = true) },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Recargar consultas")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.recommendations) { result ->
                            MatchmakingCardItem(
                                result = result,
                                useGeolocation = uiState.useGeolocation,
                                onClick = {
                                    result.user?.let { user ->
                                        viewModel.openTransactionDialog(user)
                                    }
                                }
                            )
                        }

                        if (!uiState.isAllOffersMode && uiState.totalRecommendations > 5) {
                            item {
                                OutlinedButton(
                                    onClick = { viewModel.switchToAllOffersMode() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Ver todas las ofertas (${uiState.totalRecommendations} ofertas)")
                                }
                            }
                        }

                        if (uiState.isAllOffersMode && uiState.recommendations.size < uiState.totalRecommendations) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isLoadingNextPage) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                    } else {
                                        OutlinedButton(
                                            onClick = { viewModel.loadNextPage() }
                                        ) {
                                            Text("Cargar más ofertas")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo multinivel de creación de transacción
        if (uiState.transactionState.isOpen) {
            CreateTransactionDialog(
                viewModel = viewModel,
                txState = uiState.transactionState
            )
        }
    }
}

@Composable
fun GeoModeSwitchCard(
    useGeolocation: Boolean,
    useUserCommune: Boolean,
    isLoadingGeoMode: Boolean,
    onGeoModeToggle: (Boolean) -> Unit,
    onUserCommuneToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Modo Geolocalización",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (useGeolocation) "Buscar ofertas por cercanía (GPS activo)" else "Búsqueda estándar desactivada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                if (isLoadingGeoMode) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Switch(
                        checked = useGeolocation,
                        onCheckedChange = onGeoModeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            val isCommuneSwitchEnabled = !useGeolocation && !isLoadingGeoMode

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = if (isCommuneSwitchEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Solo mi comuna",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isCommuneSwitchEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (useGeolocation) "Desactiva el modo geolocalización para usar este filtro" else if (useUserCommune) "Mostrando solo ofertas en tu comuna" else "Mostrar ofertas de todas las comunas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Switch(
                    checked = useUserCommune && !useGeolocation,
                    onCheckedChange = onUserCommuneToggle,
                    enabled = isCommuneSwitchEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    }
}

@Composable
fun HomeHeader(
    isAllOffersMode: Boolean,
    totalOffers: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAllOffersMode) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Todas las Ofertas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$totalOffers ofertas encontradas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            Column {
                Text(
                    text = "Recomendaciones para ti",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ofertas de intercambio encontradas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun MatchmakingCardItem(
    result: GetMatchmakingResponse.Data.Result,
    useGeolocation: Boolean,
    onClick: () -> Unit = {}
) {
    val primaryCard = result.matchingCards?.firstOrNull()
    val locale = LocalConfiguration.current.locales[0]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = result.user?.username ?: "Ofertador",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Puntuación",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${result.user?.averageScore ?: 0.0f}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                if (useGeolocation && result.distance != null) {
                    val distKm = result.distance / 1000.0
                    val rawDistStr = if (result.distance < 1000) "${result.distance} m" else String.format(locale, "%.1f km", distKm)
                    val formattedDist = if (result.distanceAprox == true) "a menos de $rawDistStr" else "a $rawDistStr"

                    AssistChip(
                        onClick = {},
                        label = { Text(formattedDist, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardImageThumbnail(
                    model = primaryCard?.miniatureUrl?.takeIf { it.isNotBlank() } ?: primaryCard?.imageUrl,
                    contentDescription = primaryCard?.cardName,
                    modifier = Modifier.size(70.dp, 100.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = primaryCard?.cardName ?: "Sin nombre de carta",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Set: ${primaryCard?.set.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Estado: ${primaryCard?.condition.orEmpty()} (${primaryCard?.language.orEmpty()})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = "Copias: ${primaryCard?.quantity ?: 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = "$${primaryCard?.price ?: 0}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            val otherMatchingCardsCount = (result.matchingCards?.size ?: 1) - 1
            val reciprocatedUnits = result.summary?.reciprocatedUnits ?: 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (otherMatchingCardsCount > 0) {
                    AssistChip(
                        onClick = {},
                        label = { Text("+$otherMatchingCardsCount coincidentes", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }

                if (reciprocatedUnits > 0) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Posees $reciprocatedUnits que busca", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionDialog(
    viewModel: HomeViewModel,
    txState: TransactionState
) {
    val username = txState.targetUser?.username ?: "Ofertador"

    AlertDialog(
        onDismissRequest = {
            if (!txState.isSubmittingTransaction) {
                viewModel.closeTransactionDialog()
            }
        },
        title = {
            Text(
                text = when (txState.step) {
                    1 -> "Paso 1: Solicitar cartas"
                    2 -> "Paso 2: Ofrecer de tu inventario"
                    3 -> "Paso 3: Resumen de Transacción"
                    else -> "Resultado de la Transacción"
                },
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                if (txState.isLoadingInventories) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (txState.step == 4) {
                    // Paso 4: Éxito o Error Final
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (txState.transactionSuccessMessage != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = txState.transactionSuccessMessage,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        } else if (txState.transactionError != null) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = txState.transactionError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else when (txState.step) {
                    1 -> Step1SolicitedCardsView(viewModel = viewModel, txState = txState, username = username)
                    2 -> Step2OfferedCardsView(viewModel = viewModel, txState = txState)
                    3 -> Step3SummaryView(viewModel = viewModel, txState = txState, username = username)
                }
            }
        },
        confirmButton = {
            if (txState.step == 4) {
                if (txState.transactionSuccessMessage != null) {
                    Button(onClick = { viewModel.closeTransactionDialog() }) {
                        Text("Aceptar")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.closeTransactionDialog() }) {
                            Text("Cancelar")
                        }
                        Button(onClick = { viewModel.retryTransaction() }) {
                            Text("Volver a intentarlo")
                        }
                    }
                }
            } else when (txState.step) {
                1 -> {
                    Button(
                        onClick = { viewModel.goToTransactionStep(2) },
                        enabled = txState.selectedSolicitedCards.isNotEmpty()
                    ) {
                        Text("Siguiente")
                    }
                }
                2 -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.goToTransactionStep(1) }) {
                            Text("Atrás")
                        }
                        Button(onClick = { viewModel.goToTransactionStep(3) }) {
                            Text("Ver Resumen")
                        }
                    }
                }
                3 -> {
                    Button(
                        onClick = { viewModel.submitTransaction() },
                        enabled = !txState.isSubmittingTransaction && txState.selectedSolicitedCards.isNotEmpty()
                    ) {
                        if (txState.isSubmittingTransaction) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Confirmar Oferta")
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (txState.step != 4) {
                TextButton(
                    onClick = { viewModel.closeTransactionDialog() },
                    enabled = !txState.isSubmittingTransaction
                ) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun Step1SolicitedCardsView(
    viewModel: HomeViewModel,
    txState: TransactionState,
    username: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Cartas disponibles de @$username:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (txState.otherUserInventory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "El usuario no tiene cartas disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(txState.otherUserInventory) { card ->
                    val id = card.inventoryCardId ?: return@items
                    val selectedItem = txState.selectedSolicitedCards[id]
                    val isSelected = selectedItem != null

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleSolicitedCardSelection(card, checked)
                                    }
                                )
                                CardImageThumbnail(
                                    model = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl,
                                    contentDescription = card.cardName,
                                    modifier = Modifier.size(45.dp, 65.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = card.cardName.orEmpty(),
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${card.set.orEmpty()} • ${card.condition.orEmpty()} (${card.language.orEmpty()})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "Stock: ${card.availableStock ?: 1} | Precio original: $${card.price ?: 0}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (card.inWishlist == true) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "En tu lista de deseos",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            // Controles de cantidad y precio ofertado (contraoferta)
                            if (isSelected && selectedItem != null) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Control de Cantidad
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Cant: ", style = MaterialTheme.typography.labelMedium)
                                        IconButton(
                                            onClick = {
                                                viewModel.updateSolicitedCardQuantity(id, selectedItem.quantity - 1)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "${selectedItem.quantity}",
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                viewModel.updateSolicitedCardQuantity(id, selectedItem.quantity + 1)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    // Campo de Contraoferta (Precio)
                                    OutlinedTextField(
                                        value = "${selectedItem.offeredPrice}",
                                        onValueChange = { input ->
                                            val newPrice = input.toIntOrNull() ?: 0
                                            viewModel.updateSolicitedCardOfferedPrice(id, newPrice)
                                        },
                                        label = { Text("Precio c/u ($)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(130.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Step2OfferedCardsView(
    viewModel: HomeViewModel,
    txState: TransactionState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Tus cartas para ofrecer (Opcional - Compra Pura si no seleccionas):",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (txState.myInventory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes cartas disponibles en tu inventario.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(txState.myInventory) { card ->
                    val id = card.inventoryCardId ?: return@items
                    val selectedItem = txState.selectedOfferedCards[id]
                    val isSelected = selectedItem != null

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleOfferedCardSelection(card, checked)
                                    }
                                )
                                CardImageThumbnail(
                                    model = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl,
                                    contentDescription = card.cardName,
                                    modifier = Modifier.size(45.dp, 65.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = card.cardName.orEmpty(),
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${card.set.orEmpty()} • ${card.condition.orEmpty()} (${card.language.orEmpty()})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "Stock: ${card.availableStock ?: 1} | Precio: $${card.price ?: 0}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if ((card.desiredAmount ?: 0) > 0 || card.inWishlist == true) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "¡El ofertador desea esta carta!",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            if (isSelected && selectedItem != null) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Cantidad a ofrecer: ", style = MaterialTheme.typography.labelMedium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                viewModel.updateOfferedCardQuantity(id, selectedItem.quantity - 1)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "${selectedItem.quantity}",
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                viewModel.updateOfferedCardQuantity(id, selectedItem.quantity + 1)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Step3SummaryView(
    viewModel: HomeViewModel,
    txState: TransactionState,
    username: String
) {
    val totalSolicitedValue = txState.selectedSolicitedCards.values.sumOf { it.quantity * it.offeredPrice }
    val totalSolicitedCount = txState.selectedSolicitedCards.values.sumOf { it.quantity }

    val totalOfferedValue = txState.selectedOfferedCards.values.sumOf { it.quantity * (it.card.price ?: 0) }
    val totalOfferedCount = txState.selectedOfferedCards.values.sumOf { it.quantity }

    val diff = totalSolicitedValue - totalOfferedValue

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Resumen Solicitadas
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Solicitadas a @$username", style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { viewModel.goToTransactionStep(1) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar solicitadas", modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Total cartas: $totalSolicitedCount", style = MaterialTheme.typography.bodySmall)
                Text("Valor total: $$totalSolicitedValue", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        // Resumen Ofrecidas
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ofrecidas por ti", style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { viewModel.goToTransactionStep(2) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar ofrecidas", modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (totalOfferedCount == 0) {
                    Text("Compra pura (sin cartas ofrecidas)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                } else {
                    Text("Total cartas: $totalOfferedCount", style = MaterialTheme.typography.bodySmall)
                    Text("Valor total: $$totalOfferedValue", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Balance y Diferencia Moneda
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Balance de la Transacción",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))

                val balanceText = when {
                    diff > 0 -> "Debes pagar $$diff a @$username"
                    diff < 0 -> "@$username te debe $${abs(diff)}"
                    else -> "Intercambio equitativo (sin diferencia monetaria)"
                }

                Text(
                    text = balanceText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }

        txState.transactionError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
            text = "Error al cargar las ofertas",
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

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private fun fetchLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationObtained: (Float, Float) -> Unit
) {
    if (!hasLocationPermission(context)) return

    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
        if (lastLoc != null) {
            onLocationObtained(lastLoc.latitude.toFloat(), lastLoc.longitude.toFloat())
        } else {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    onLocationObtained(location.latitude.toFloat(), location.longitude.toFloat())
                }
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }
        }
    }.addOnFailureListener {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                onLocationObtained(location.latitude.toFloat(), location.longitude.toFloat())
            }
        }
    }
}
