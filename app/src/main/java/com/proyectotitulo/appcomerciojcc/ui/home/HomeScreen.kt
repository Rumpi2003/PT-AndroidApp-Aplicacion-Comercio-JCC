package com.proyectotitulo.appcomerciojcc.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.filled.StarOutline
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
import com.proyectotitulo.appcomerciojcc.domain.models.GetMostPopularCardsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTradeInventoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.HomeUiState
import com.proyectotitulo.appcomerciojcc.domain.models.TransactionState
import com.proyectotitulo.appcomerciojcc.ui.components.CardImageThumbnail
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onTransactionCreated: () -> Unit = {},
    onUserClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.observeAsState(initial = HomeUiState())
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Interceptar el botón físico/gesto Atrás del teléfono al estar en modo "Todas las Ofertas"
    BackHandler(enabled = uiState.isAllOffersMode) {
        viewModel.exitAllOffersMode()
    }

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

    // Bucle periódico de envío de ubicación cada 30 segundos mientras el modo geolocalización esté activo
    LaunchedEffect(uiState.useGeolocation) {
        if (uiState.useGeolocation && hasLocationPermission(context)) {
            while (isActive && uiState.useGeolocation) {
                requestLocationAndSend { lat, lng ->
                    viewModel.sendLocationAndUpdateMatchmaking(lat, lng)
                }
                delay(30000)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                if (uiState.isAllOffersMode) {
                    viewModel.loadAllOffers(forceRefresh = true)
                } else {
                    viewModel.loadRecommendations(forceRefresh = true)
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeHeader(
                    isAllOffersMode = uiState.isAllOffersMode,
                    totalOffers = uiState.totalRecommendations,
                    onBackClick = { viewModel.exitAllOffersMode() }
                )

                // Módulo de Popularidad (Top 10 Cartas más Populares del Mes)
                if (!uiState.isAllOffersMode) {
                    PopularCardsSection(
                        popularCards = uiState.popularCards,
                        isLoading = uiState.isLoadingPopularCards
                    )
                }

                FilterSwitchesSection(
                    useUserCommune = uiState.useUserCommune,
                    useGeolocation = uiState.useGeolocation,
                    isCommuneSwitchEnabled = !uiState.useGeolocation,
                    onUserCommuneToggle = { enabled -> viewModel.toggleUserCommuneFilter(enabled) },
                    onGeolocationToggle = { enabled ->
                        viewModel.toggleGeolocationFilter(enabled) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
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
                        onRetry = {
                            if (uiState.isAllOffersMode) {
                                viewModel.loadAllOffers(forceRefresh = true)
                            } else {
                                viewModel.loadRecommendations(forceRefresh = true)
                            }
                        }
                    )
                } else {
                    val currentList = uiState.recommendations

                    if (currentList.isEmpty()) {
                        EmptyContent(
                            isAllOffersMode = uiState.isAllOffersMode,
                            useUserCommune = uiState.useUserCommune,
                            useGeolocation = uiState.useGeolocation,
                            onRefresh = {
                                if (uiState.isAllOffersMode) {
                                    viewModel.loadAllOffers(forceRefresh = true)
                                } else {
                                    viewModel.loadRecommendations(forceRefresh = true)
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentList) { result ->
                                MatchmakingCardItem(
                                    result = result,
                                    onInitiateTransaction = { user ->
                                        viewModel.openTransactionDialog(user)
                                    },
                                    onUserClick = onUserClick
                                )
                            }

                            // Mostrar el botón de ver todas las ofertas sólo si no estamos en el modo de todas las ofertas y hay más de 5
                            if (!uiState.isAllOffersMode && uiState.totalRecommendations > 5) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.loadAllOffers(forceRefresh = true) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text("Ver todas las ofertas (${uiState.totalRecommendations} ofertas)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.transactionState.isOpen) {
            TransactionDialog(
                viewModel = viewModel,
                txState = uiState.transactionState,
                onTransactionCreated = onTransactionCreated
            )
        }
    }
}

@Composable
fun PopularCardsSection(
    popularCards: List<GetMostPopularCardsResponse.Card>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (popularCards.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Top 10 Cartas Más Populares del Mes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(popularCards) { index, card ->
                    PopularCardItem(
                        rank = index + 1,
                        card = card
                    )
                }
            }
        }
    }
}

@Composable
fun PopularCardItem(
    rank: Int,
    card: GetMostPopularCardsResponse.Card
) {
    Card(
        modifier = Modifier
            .width(180.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("#$rank Top", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            CardImageThumbnail(
                model = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl,
                contentDescription = card.cardName,
                modifier = Modifier.size(75.dp, 105.dp)
            )

            Text(
                text = card.cardName.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = card.set.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = card.rarity.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Estadísticas (transacciones completadas y usuarios que la desean)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${card.completedTransactions ?: 0} intercambios",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Deseada por ${card.wantedByUsers ?: 0}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FilterSwitchesSection(
    useUserCommune: Boolean,
    useGeolocation: Boolean,
    isCommuneSwitchEnabled: Boolean,
    onUserCommuneToggle: (Boolean) -> Unit,
    onGeolocationToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Modo Geolocalización GPS",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Busca ofertas en tu radio de distancia actual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Switch(
                    checked = useGeolocation,
                    onCheckedChange = onGeolocationToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Filtro por Comuna Registrada",
                            style = MaterialTheme.typography.titleSmall,
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
    onInitiateTransaction: (GetMatchmakingResponse.Data.Result.User) -> Unit,
    onUserClick: (Int) -> Unit = {}
) {
    val username = result.user?.username ?: "Usuario"
    val averageScore = result.user?.averageScore ?: 0f
    val filledStars = averageScore.toInt().coerceIn(0, 5)
    val matchingCards = result.matchingCards ?: emptyList()
    val firstCard = matchingCards.firstOrNull()
    val reciprocatedUnits = result.summary?.reciprocatedUnits ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cabecera: Avatar, Username, Puntuación de 5 Estrellas y Distancia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        result.user?.userId?.let { uid -> onUserClick(uid) }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "@$username",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (averageScore > 0) {
                                repeat(5) { index ->
                                    val icon = if (index < filledStars) Icons.Default.Star else Icons.Default.StarOutline
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", averageScore),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } else {
                                Text(
                                    text = "Sin calificaciones",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            result.distance?.let { m ->
                                val km = m / 1000f
                                Text(
                                    text = " • ${String.format(Locale.getDefault(), "%.1f", km)} km",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                result.user?.let { targetUser ->
                    Button(
                        onClick = { onInitiateTransaction(targetUser) },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ofertar")
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Primera Carta Coincidente que ofrece el otro usuario y que buscas
            if (firstCard != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CardImageThumbnail(
                        model = firstCard.miniatureUrl?.takeIf { it.isNotBlank() } ?: firstCard.imageUrl,
                        contentDescription = firstCard.cardName,
                        modifier = Modifier.size(45.dp, 65.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = firstCard.cardName.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${firstCard.set.orEmpty()} • ${firstCard.condition.orEmpty()} (${firstCard.language.orEmpty()})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Stock: ${firstCard.quantity ?: 1} | Precio: $${firstCard.price ?: 0}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (matchingCards.size > 1) {
                    Text(
                        text = "+ ${matchingCards.size - 1} cartas más que buscas de este usuario",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "No se encontraron cartas en común directamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Indicador de Cartas Recíprocas (Tú tienes N cartas que este usuario busca)
            if (reciprocatedUnits > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tú tienes $reciprocatedUnits carta${if (reciprocatedUnits > 1) "s" else ""} que @$username busca en su lista de deseos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    viewModel: HomeViewModel,
    txState: TransactionState,
    onTransactionCreated: () -> Unit = {}
) {
    val username = txState.targetUser?.username ?: "Usuario"

    AlertDialog(
        onDismissRequest = {
            if (!txState.isSubmittingTransaction) {
                viewModel.closeTransactionDialog()
            }
        },
        title = {
            Text(
                text = when (txState.step) {
                    1 -> "Seleccionar Solicitadas de @$username"
                    2 -> "Seleccionar Ofrecidas de tu Inventario"
                    3 -> "Resumen de la Transacción con @$username"
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
                    1 -> Step1SolicitedView(viewModel = viewModel, txState = txState)
                    2 -> Step2OfferedView(viewModel = viewModel, txState = txState)
                    3 -> Step3SummaryView(viewModel = viewModel, txState = txState, username = username)
                }
            }
        },
        confirmButton = {
            if (txState.step == 4) {
                Button(onClick = { viewModel.closeTransactionDialog() }) {
                    Text("Aceptar")
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
                        onClick = { viewModel.submitTransaction(onSuccess = onTransactionCreated) },
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
                    Text("Cartas Solicitadas a @$username", style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { viewModel.goToTransactionStep(1) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar solicitadas", modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Total cartas: $totalSolicitedCount", style = MaterialTheme.typography.bodySmall)
                Text("Valor total: $$totalSolicitedValue", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

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
                    Text("Cartas Ofrecidas de tu Inventario", style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { viewModel.goToTransactionStep(2) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar ofrecidas", modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (totalOfferedCount == 0) {
                    Text("Sin cartas ofrecidas (Compra pura)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                } else {
                    Text("Total cartas: $totalOfferedCount", style = MaterialTheme.typography.bodySmall)
                    Text("Valor total: $$totalOfferedValue", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Balance Estimado de la Oferta",
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
fun Step1SolicitedView(
    viewModel: HomeViewModel,
    txState: TransactionState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Selecciona las cartas que deseas solicitar:",
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
                    text = "El usuario no tiene cartas disponibles en su inventario.",
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
                                        text = "Stock: ${card.availableStock ?: 1} | Precio: $${card.price ?: 0}",
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

                            if (isSelected && selectedItem != null) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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

                                    OutlinedTextField(
                                        value = "${selectedItem.offeredPrice}",
                                        onValueChange = { input ->
                                            val newPrice = input.toIntOrNull() ?: 0
                                            viewModel.updateSolicitedCardPrice(id, newPrice)
                                        },
                                        label = { Text("Precio ofertado c/u ($)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(140.dp)
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
fun Step2OfferedView(
    viewModel: HomeViewModel,
    txState: TransactionState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Selecciona las cartas que ofreces a cambio (opcional):",
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
                                        text = "Stock: ${card.availableStock ?: 1} | Precio tu carta: $${card.price ?: 0}",
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
                                                text = "¡Este usuario desea esta carta!",
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Cantidad: ", style = MaterialTheme.typography.labelMedium)
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
fun EmptyContent(
    isAllOffersMode: Boolean,
    useUserCommune: Boolean,
    useGeolocation: Boolean,
    onRefresh: () -> Unit = {}
) {
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
            Icon(
                imageVector = Icons.Default.Style,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (useGeolocation) "No hay ofertas cercanas a tu ubicación actual."
                else if (useUserCommune) "No se encontraron ofertas disponibles en tu comuna."
                else "No hay ofertas disponibles en este momento.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Prueba desactivando los filtros o agregando más cartas a tu lista de deseos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onRefresh,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recargar ofertas")
            }
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
            text = "Error al obtener recomendaciones",
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
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@SuppressLint("MissingPermission")
private fun fetchLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationObtained: (Float, Float) -> Unit
) {
    if (!hasLocationPermission(context)) return

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        CancellationTokenSource().token
    ).addOnSuccessListener { location ->
        if (location != null) {
            onLocationObtained(location.latitude.toFloat(), location.longitude.toFloat())
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    onLocationObtained(lastLoc.latitude.toFloat(), lastLoc.longitude.toFloat())
                }
            }
        }
    }
}
