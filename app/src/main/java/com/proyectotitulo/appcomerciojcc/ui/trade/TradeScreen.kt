package com.proyectotitulo.appcomerciojcc.ui.trade

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.proyectotitulo.appcomerciojcc.domain.models.GetTransactionsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.NegotiationState
import com.proyectotitulo.appcomerciojcc.domain.models.TradeUiState
import com.proyectotitulo.appcomerciojcc.ui.components.CardImageThumbnail
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeScreen(
    modifier: Modifier = Modifier,
    viewModel: TradeViewModel
) {
    val uiState by viewModel.uiState.observeAsState(initial = TradeUiState())

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                if (uiState.isHistoryView) {
                    viewModel.loadHistoryTransactions(forceRefresh = true)
                } else {
                    viewModel.loadActiveTransactions(forceRefresh = true)
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isHistoryView) {
                HistoryTransactionsContent(viewModel = viewModel, uiState = uiState)
            } else {
                ActiveTransactionsContent(viewModel = viewModel, uiState = uiState)
            }
        }

        // Modal de Negociación (Aceptar / Rechazar / Contraoferta)
        if (uiState.negotiationState.isOpen) {
            NegotiationDialog(
                viewModel = viewModel,
                negState = uiState.negotiationState
            )
        }
    }
}

@Composable
fun ActiveTransactionsContent(
    viewModel: TradeViewModel,
    uiState: TradeUiState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Transacciones",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Gestiona tus intercambios y negociaciones activas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

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
                onRetry = { viewModel.loadActiveTransactions(forceRefresh = true) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección 1: Transacciones Pendientes (máx 5)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transacciones Pendientes",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("${uiState.pendingTransactions.size}/5", style = MaterialTheme.typography.labelSmall) }
                            )
                        }

                        if (uiState.pendingTransactions.isEmpty()) {
                            Text(
                                text = "No tienes transacciones pendientes en este momento.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            uiState.pendingTransactions.forEach { tx ->
                                TransactionCardItem(
                                    result = tx,
                                    onClick = {
                                        if (tx.myTurn == true && tx.transactionId != null && tx.counterpart != null) {
                                            viewModel.openNegotiationDialog(tx.transactionId, tx.counterpart)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Sección 2: En Negociación - Iniciadas por ti (PROPONENTE - máx 10)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "En Negociación (Iniciadas por ti)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("${uiState.myNegotiatingTransactions.size}/10", style = MaterialTheme.typography.labelSmall) }
                            )
                        }

                        if (uiState.myNegotiatingTransactions.isEmpty()) {
                            Text(
                                text = "No has iniciado negociaciones activas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            uiState.myNegotiatingTransactions.forEach { tx ->
                                TransactionCardItem(
                                    result = tx,
                                    onClick = {
                                        if (tx.myTurn == true && tx.transactionId != null && tx.counterpart != null) {
                                            viewModel.openNegotiationDialog(tx.transactionId, tx.counterpart)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Sección 3: En Negociación - Ofertas Recibidas (DESTINATARIO - Ilimitadas, paginación horizontal)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "En Negociación (Ofertas Recibidas)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("${uiState.receivedNegotiatingTransactions.size} activas", style = MaterialTheme.typography.labelSmall) }
                            )
                        }

                        if (uiState.receivedNegotiatingTransactions.isEmpty()) {
                            Text(
                                text = "No has recibido ofertas de negociación de otros usuarios.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.receivedNegotiatingTransactions) { tx ->
                                    TransactionCardItem(
                                        result = tx,
                                        modifier = Modifier.width(340.dp),
                                        onClick = {
                                            if (tx.myTurn == true && tx.transactionId != null && tx.counterpart != null) {
                                                viewModel.openNegotiationDialog(tx.transactionId, tx.counterpart)
                                            }
                                        }
                                    )
                                }

                                if (uiState.receivedNegotiatingTransactions.size < uiState.receivedTotal) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .width(140.dp)
                                                .fillMaxHeight(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (uiState.isLoadingMoreReceived) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                            } else {
                                                OutlinedButton(
                                                    onClick = { viewModel.loadMoreReceivedNegotiating() }
                                                ) {
                                                    Text("Cargar más")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Botón Inferior: Ver Historial
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.switchToHistoryView() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver historial de transacciones")
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTransactionsContent(
    viewModel: TradeViewModel,
    uiState: TradeUiState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.exitHistoryView() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Historial de Transacciones",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Transacciones pasadas, completadas o canceladas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedHistoryFilter == null,
                onClick = { viewModel.filterHistoryByState(null) },
                label = { Text("Todas") }
            )
            FilterChip(
                selected = uiState.selectedHistoryFilter == "COMPLETADA",
                onClick = { viewModel.filterHistoryByState("COMPLETADA") },
                label = { Text("Completadas") }
            )
            FilterChip(
                selected = uiState.selectedHistoryFilter == "CANCELADA",
                onClick = { viewModel.filterHistoryByState("CANCELADA") },
                label = { Text("Canceladas") }
            )
            FilterChip(
                selected = uiState.selectedHistoryFilter == "RECHAZADA",
                onClick = { viewModel.filterHistoryByState("RECHAZADA") },
                label = { Text("Rechazadas") }
            )
        }

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
                onRetry = { viewModel.loadHistoryTransactions(forceRefresh = true) }
            )
        } else if (uiState.historyTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron transacciones en el historial.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.historyTransactions) { tx ->
                    TransactionCardItem(result = tx)
                }

                if (uiState.historyTransactions.size < uiState.historyTotal) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoadingMoreHistory) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.loadHistoryTransactions(forceRefresh = false) }
                                ) {
                                    Text("Cargar más historial")
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
fun TransactionCardItem(
    result: GetTransactionsResponse.Data.Result,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val state = result.transactionState.orEmpty()
    val role = result.role.orEmpty()
    val username = result.counterpart?.username ?: "Contraparte"
    val isMyTurn = result.myTurn == true

    val stateColor = when (state) {
        "COMPLETADA" -> MaterialTheme.colorScheme.primary
        "PENDIENTE" -> MaterialTheme.colorScheme.tertiary
        "EN_NEGOCIACION" -> MaterialTheme.colorScheme.secondary
        "CANCELADA", "RECHAZADA" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    val stateText = when (state) {
        "COMPLETADA" -> "Completada"
        "PENDIENTE" -> "Pendiente"
        "EN_NEGOCIACION" -> "En Negociación"
        "CANCELADA" -> "Cancelada"
        "RECHAZADA" -> "Rechazada"
        else -> state
    }

    val roleText = if (role == "PROPONENTE") "Propuesto por ti" else "Enviado a ti"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isMyTurn) { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "@$username",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = roleText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                AssistChip(
                    onClick = {},
                    label = { Text(stateText, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = stateColor.copy(alpha = 0.15f),
                        labelColor = stateColor
                    )
                )
            }

            // Insignia de Turno Activo
            if (isMyTurn) {
                AssistChip(
                    onClick = {},
                    label = { Text("🟢 Tu turno para responder", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Entregas: ${result.summary?.deliveredUnits ?: 0} cartas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Valor: $${result.summary?.deliveredValue ?: 0}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Recibes: ${result.summary?.receivedUnits ?: 0} cartas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Valor: $${result.summary?.receivedValue ?: 0}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            val myDebt = result.summary?.myDebt ?: 0
            val counterpartDebt = result.summary?.counterpartDebt ?: 0

            if (myDebt > 0 || counterpartDebt > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Diferencia de Dinero:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (myDebt > 0) "Debes pagar: $$myDebt" else "Te deben: $$counterpartDebt",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (myDebt > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegotiationDialog(
    viewModel: TradeViewModel,
    negState: NegotiationState
) {
    val username = negState.targetUser?.username ?: "Contraparte"

    AlertDialog(
        onDismissRequest = {
            if (!negState.isSubmittingAction) {
                viewModel.closeNegotiationDialog()
            }
        },
        title = {
            Text(
                text = when (negState.step) {
                    1 -> "Editar Solicitadas a @$username"
                    2 -> "Editar Ofrecidas de tu Inventario"
                    3 -> "Negociación con @$username"
                    else -> "Resultado de Negociación"
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
                if (negState.isLoadingDetails) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (negState.step == 4) {
                    // Paso 4: Éxito o Error
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (negState.negotiationSuccessMessage != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = negState.negotiationSuccessMessage,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        } else if (negState.negotiationError != null) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = negState.negotiationError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else when (negState.step) {
                    1 -> Step1NegotiationSolicitedView(viewModel = viewModel, negState = negState)
                    2 -> Step2NegotiationOfferedView(viewModel = viewModel, negState = negState)
                    3 -> Step3NegotiationSummaryView(viewModel = viewModel, negState = negState, username = username)
                }
            }
        },
        confirmButton = {
            if (negState.step == 4) {
                Button(onClick = { viewModel.closeNegotiationDialog() }) {
                    Text("Aceptar")
                }
            } else when (negState.step) {
                1, 2 -> {
                    Button(onClick = { viewModel.goToNegotiationStep(3) }) {
                        Text("Aceptar y Volver al Resumen")
                    }
                }
                3 -> {
                    if (negState.hasModifications) {
                        Button(
                            onClick = { viewModel.submitCounterOffer() },
                            enabled = !negState.isSubmittingAction && (negState.selectedSolicitedCards.isNotEmpty() || negState.selectedOfferedCards.isNotEmpty())
                        ) {
                            if (negState.isSubmittingAction) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Enviar Contraoferta")
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.declineNegotiation() },
                                enabled = !negState.isSubmittingAction,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Rechazar Oferta")
                            }
                            Button(
                                onClick = { viewModel.acceptNegotiation() },
                                enabled = !negState.isSubmittingAction
                            ) {
                                if (negState.isSubmittingAction) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Aceptar Oferta")
                                }
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (negState.step != 4) {
                TextButton(
                    onClick = { viewModel.closeNegotiationDialog() },
                    enabled = !negState.isSubmittingAction
                ) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun Step3NegotiationSummaryView(
    viewModel: TradeViewModel,
    negState: NegotiationState,
    username: String
) {
    val totalSolicitedValue = negState.selectedSolicitedCards.values.sumOf { it.quantity * it.offeredPrice }
    val totalSolicitedCount = negState.selectedSolicitedCards.values.sumOf { it.quantity }

    val totalOfferedValue = negState.selectedOfferedCards.values.sumOf { it.quantity * it.offeredPrice }
    val totalOfferedCount = negState.selectedOfferedCards.values.sumOf { it.quantity }

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
                    IconButton(onClick = { viewModel.goToNegotiationStep(1) }, modifier = Modifier.size(24.dp)) {
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
                    IconButton(onClick = { viewModel.goToNegotiationStep(2) }, modifier = Modifier.size(24.dp)) {
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
                    text = "Balance de la Negociación",
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

        negState.negotiationError?.let { error ->
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
fun Step1NegotiationSolicitedView(
    viewModel: TradeViewModel,
    negState: NegotiationState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Modifica las cartas que pides al ofertador:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (negState.otherUserInventory.isEmpty()) {
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
                items(negState.otherUserInventory) { card ->
                    val id = card.inventoryCardId ?: return@items
                    val selectedItem = negState.selectedSolicitedCards[id]
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
                                        viewModel.toggleNegotiationSolicitedCardSelection(card, checked)
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
                                                viewModel.updateNegotiationSolicitedCardQuantity(id, selectedItem.quantity - 1)
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
                                                viewModel.updateNegotiationSolicitedCardQuantity(id, selectedItem.quantity + 1)
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
                                            viewModel.updateNegotiationSolicitedCardPrice(id, newPrice)
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
fun Step2NegotiationOfferedView(
    viewModel: TradeViewModel,
    negState: NegotiationState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Modifica las cartas que ofreces de tu inventario:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (negState.myInventory.isEmpty()) {
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
                items(negState.myInventory) { card ->
                    val id = card.inventoryCardId ?: return@items
                    val selectedItem = negState.selectedOfferedCards[id]
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
                                        viewModel.toggleNegotiationOfferedCardSelection(card, checked)
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Cant: ", style = MaterialTheme.typography.labelMedium)
                                        IconButton(
                                            onClick = {
                                                viewModel.updateNegotiationOfferedCardQuantity(id, selectedItem.quantity - 1)
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
                                                viewModel.updateNegotiationOfferedCardQuantity(id, selectedItem.quantity + 1)
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
                                            viewModel.updateNegotiationOfferedCardPrice(id, newPrice)
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
            text = "Error al cargar las transacciones",
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
