package com.proyectotitulo.appcomerciojcc.ui.trade

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Style
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.proyectotitulo.appcomerciojcc.domain.models.CancelConfirmationState
import com.proyectotitulo.appcomerciojcc.domain.models.ChatState
import com.proyectotitulo.appcomerciojcc.domain.models.GetTransactionsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.NegotiationState
import com.proyectotitulo.appcomerciojcc.domain.models.PendingSummaryState
import com.proyectotitulo.appcomerciojcc.domain.models.ReviewState
import com.proyectotitulo.appcomerciojcc.domain.models.TradeUiState
import com.proyectotitulo.appcomerciojcc.domain.models.WaitingRoomState
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

        // Pantalla Completa de Chat en Tiempo Real
        if (uiState.chatState.isOpen) {
            ChatFullScreenView(
                viewModel = viewModel,
                chatState = uiState.chatState
            )
        }

        // Diálogo de Resumen Completo de la Transacción Pendiente
        if (uiState.pendingSummaryState.isOpen) {
            PendingTransactionSummaryDialog(
                viewModel = viewModel,
                summaryState = uiState.pendingSummaryState
            )
        }

        // Diálogo de Confirmación de Cancelación
        if (uiState.cancelState.isOpen) {
            CancelTransactionDialog(
                viewModel = viewModel,
                cancelState = uiState.cancelState
            )
        }

        // Sala de Espera al Completar Transacción
        if (uiState.waitingRoomState.isOpen) {
            WaitingRoomDialog(
                viewModel = viewModel,
                waitingState = uiState.waitingRoomState
            )
        }

        // Diálogo para Preguntar si Desea Editar Reseña Existente
        if (uiState.reviewState.isAskToEditOpen) {
            AskToEditReviewDialog(
                viewModel = viewModel,
                reviewState = uiState.reviewState
            )
        }

        // Diálogo para Escribir / Editar Reseña
        if (uiState.reviewState.isOpen) {
            WriteReviewDialog(
                viewModel = viewModel,
                reviewState = uiState.reviewState
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
                                    },
                                    onOpenChat = { txId, uname, counterpartUserId ->
                                        viewModel.openChat(txId, uname, counterpartUserId)
                                    },
                                    onOpenPendingSummary = { txId ->
                                        viewModel.openPendingSummary(txId)
                                    },
                                    onCancelTransaction = { txId, uname ->
                                        viewModel.openCancelConfirmation(txId, uname)
                                    },
                                    onCompleteTransaction = { txId, uname, counterpartUserId ->
                                        viewModel.completeTransaction(txId, uname, counterpartUserId)
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
                                    },
                                    onOpenChat = { txId, uname, counterpartUserId ->
                                        viewModel.openChat(txId, uname, counterpartUserId)
                                    },
                                    onOpenPendingSummary = { txId ->
                                        viewModel.openPendingSummary(txId)
                                    },
                                    onCancelTransaction = { txId, uname ->
                                        viewModel.openCancelConfirmation(txId, uname)
                                    },
                                    onCompleteTransaction = { txId, uname, counterpartUserId ->
                                        viewModel.completeTransaction(txId, uname, counterpartUserId)
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
                                text = "En Negociación (Recibidas)",
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
                                        },
                                        onOpenChat = { txId, uname, counterpartUserId ->
                                            viewModel.openChat(txId, uname, counterpartUserId)
                                        },
                                        onOpenPendingSummary = { txId ->
                                            viewModel.openPendingSummary(txId)
                                        },
                                        onCancelTransaction = { txId, uname ->
                                            viewModel.openCancelConfirmation(txId, uname)
                                        },
                                        onCompleteTransaction = { txId, uname, counterpartUserId ->
                                            viewModel.completeTransaction(txId, uname, counterpartUserId)
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

        // Fila de chips con desplazamiento horizontal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
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
                    TransactionCardItem(
                        result = tx,
                        onOpenPendingSummary = { txId ->
                            viewModel.openPendingSummary(txId)
                        }
                    )
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
    onClick: () -> Unit = {},
    onOpenChat: (Int, String, Int) -> Unit = { _, _, _ -> },
    onOpenPendingSummary: (Int) -> Unit = {},
    onCancelTransaction: (Int, String) -> Unit = { _, _ -> },
    onCompleteTransaction: (Int, String, Int) -> Unit = { _, _, _ -> }
) {
    val state = result.transactionState.orEmpty()
    val role = result.role.orEmpty()
    val username = result.counterpart?.username ?: "Contraparte"
    val counterpartUserId = result.counterpart?.userId ?: 0
    val isMyTurn = result.myTurn == true
    val txId = result.transactionId ?: 0

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
    val canCancel = (state == "PENDIENTE" || state == "EN_NEGOCIACION") && txId != 0

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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stateText, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = stateColor.copy(alpha = 0.15f),
                            labelColor = stateColor
                        )
                    )

                    if (canCancel) {
                        IconButton(
                            onClick = { onCancelTransaction(txId, username) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar transacción",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

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

            // Acciones para Transacciones Pendientes (Ver Resumen, Abrir Chat y Completar)
            if (state == "PENDIENTE" && txId != 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onOpenPendingSummary(txId) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Resumen", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = { onOpenChat(txId, username, counterpartUserId) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Abrir Chat", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Botón de Completar Transacción bajo el Resumen y Chat
                Button(
                    onClick = { onCompleteTransaction(txId, username, counterpartUserId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Completar Transacción", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun WaitingRoomDialog(
    viewModel: TradeViewModel,
    waitingState: WaitingRoomState
) {
    AlertDialog(
        onDismissRequest = {
            if (!waitingState.isWithdrawing) {
                viewModel.withdrawCompleteTransaction()
            }
        },
        title = {
            Text("Sala de Espera", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))

                Text(
                    text = "Esperando que @${waitingState.counterpartUsername} confirme la finalización de la transacción...",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Si sales de esta sala, se cancelará tu confirmación de completado.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.withdrawCompleteTransaction() },
                enabled = !waitingState.isWithdrawing,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                if (waitingState.isWithdrawing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Salir y Cancelar Confirmación")
                }
            }
        }
    )
}

@Composable
fun AskToEditReviewDialog(
    viewModel: TradeViewModel,
    reviewState: ReviewState
) {
    AlertDialog(
        onDismissRequest = { viewModel.closeReviewState() },
        title = {
            Text("Reseña Existente", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(
                text = "Ya has escrito una reseña para la transacción con @${reviewState.counterpartUsername}. ¿Deseas editarla?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = { viewModel.proceedToEditExistingReview() }) {
                Text("Editar Reseña")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeReviewState() }) {
                Text("Mantener como está")
            }
        }
    )
}

@Composable
fun WriteReviewDialog(
    viewModel: TradeViewModel,
    reviewState: ReviewState
) {
    AlertDialog(
        onDismissRequest = {
            if (!reviewState.isSubmittingReview) {
                viewModel.closeReviewState()
            }
        },
        title = {
            Text(
                text = if (reviewState.isEditingExisting) "Editar Reseña a @${reviewState.counterpartUsername}" else "Escribir Reseña a @${reviewState.counterpartUsername}",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (reviewState.isLoadingReview) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (reviewState.reviewSuccessMessage != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = reviewState.reviewSuccessMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = "Calificación (1 a 5 estrellas):",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { viewModel.updateReviewScore(star) }) {
                                Icon(
                                    imageVector = if (star <= reviewState.score) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = "$star estrellas",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reviewState.comment,
                        onValueChange = { viewModel.updateReviewComment(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Comentario (0 a 255 caracteres)") },
                        maxLines = 4,
                        trailingIcon = {
                            Text(
                                text = "${reviewState.comment.length}/255",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    )

                    reviewState.reviewError?.let { err ->
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (reviewState.reviewSuccessMessage != null) {
                Button(onClick = { viewModel.closeReviewState() }) {
                    Text("Aceptar")
                }
            } else if (!reviewState.isLoadingReview) {
                Button(
                    onClick = { viewModel.submitReview() },
                    enabled = !reviewState.isSubmittingReview
                ) {
                    if (reviewState.isSubmittingReview) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (reviewState.isEditingExisting) "Guardar Cambios" else "Enviar Reseña")
                    }
                }
            }
        },
        dismissButton = {
            if (reviewState.reviewSuccessMessage == null && !reviewState.isLoadingReview) {
                TextButton(
                    onClick = { viewModel.closeReviewState() },
                    enabled = !reviewState.isSubmittingReview
                ) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun CancelTransactionDialog(
    viewModel: TradeViewModel,
    cancelState: CancelConfirmationState
) {
    AlertDialog(
        onDismissRequest = {
            if (!cancelState.isCanceling) {
                viewModel.closeCancelConfirmation()
            }
        },
        title = {
            Text("Cancelar Transacción", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            if (cancelState.successMessage != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = cancelState.successMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¿Estás seguro de que deseas cancelar la transacción con @${cancelState.counterpartUsername}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Esta acción es irreversible y cambiará el estado de la transacción a CANCELADA.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    cancelState.errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (cancelState.successMessage != null) {
                Button(onClick = { viewModel.closeCancelConfirmation() }) {
                    Text("Aceptar")
                }
            } else {
                Button(
                    onClick = { viewModel.confirmCancelTransaction() },
                    enabled = !cancelState.isCanceling,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (cancelState.isCanceling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text("Sí, cancelar")
                    }
                }
            }
        },
        dismissButton = {
            if (cancelState.successMessage == null) {
                TextButton(
                    onClick = { viewModel.closeCancelConfirmation() },
                    enabled = !cancelState.isCanceling
                ) {
                    Text("Volver")
                }
            }
        }
    )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (negState.step) {
                        1 -> "Editar Solicitadas a @$username"
                        2 -> "Editar Ofrecidas"
                        3 -> "Negociación con @$username"
                        else -> "Resultado de Negociación"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = { viewModel.closeNegotiationDialog() },
                    enabled = !negState.isSubmittingAction
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }
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
                        Text("Volver al Resumen")
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
        },
        dismissButton = {
            if (negState.step == 3 && !negState.hasModifications) {
                OutlinedButton(
                    onClick = { viewModel.declineNegotiation() },
                    enabled = !negState.isSubmittingAction,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Rechazar Oferta")
                }
            } else if (negState.step != 4) {
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
fun ChatFullScreenView(
    viewModel: TradeViewModel,
    chatState: ChatState
) {
    val listState = rememberLazyListState()

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = { viewModel.closeChat() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val view = LocalView.current
        LaunchedEffect(Unit) {
            (view.parent as? DialogWindowProvider)?.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Top Bar de Chat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.closeChat() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "@${chatState.counterpartUsername}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (chatState.isChatClosed) "Chat cerrado" else "Coordinar reunión",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Botón para abrir el resumen de la transacción desde el chat
                    IconButton(onClick = { viewModel.openPendingSummary(chatState.transactionId) }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ver resumen de la transacción",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Área de Mensajes
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    if (chatState.isLoadingHistory) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (chatState.messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay mensajes aún. ¡Escribe para coordinar la reunión!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chatState.messages) { msg ->
                                val isMine = msg.senderId == chatState.currentUserId

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.80f)
                                            .wrapContentWidth(if (isMine) Alignment.End else Alignment.Start)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isMine) 16.dp else 2.dp,
                                                    bottomEnd = if (isMine) 2.dp else 16.dp
                                                )
                                            )
                                            .background(
                                                if (isMine) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = msg.content.orEmpty(),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.align(Alignment.End),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isMine) {
                                                    Icon(
                                                        imageVector = if (msg.isRead == true) Icons.Default.DoneAll else Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (msg.isRead == true) "Leído" else "Enviado",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
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

                chatState.chatError?.let { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }

                // Barra Inferior de Texto
                if (!chatState.isChatClosed) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatState.inputContent,
                                onValueChange = { viewModel.updateChatInput(it.take(255)) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Escribe un mensaje...") },
                                maxLines = 4,
                                shape = RoundedCornerShape(24.dp),
                                trailingIcon = {
                                    Text(
                                        text = "${chatState.inputContent.length}/255",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.sendMessage() },
                                enabled = chatState.inputContent.trim().length in 1..255 && !chatState.isSending,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (chatState.inputContent.trim().length in 1..255) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            ) {
                                if (chatState.isSending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Enviar",
                                        tint = if (chatState.inputContent.trim().length in 1..255) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Esta transacción ha sido cerrada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PendingTransactionSummaryDialog(
    viewModel: TradeViewModel,
    summaryState: PendingSummaryState
) {
    AlertDialog(
        onDismissRequest = { viewModel.closePendingSummary() },
        title = {
            Text(
                text = "Resumen de la Transacción",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                if (summaryState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (summaryState.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = summaryState.error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val details = summaryState.details
                    val cards = details?.cards ?: emptyList()
                    val receivedCards = cards.filter { it.direction == "RECIBO" }
                    val deliveredCards = cards.filter { it.direction == "ENTREGO" }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Info Usuario Contraparte y Estado
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Contraparte: @${details?.counterpart?.username ?: "Contraparte"}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Estado: ${details?.transactionState ?: "PENDIENTE"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Cartas que Recibes
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Cartas que recibes (${receivedCards.sumOf { it.quantity ?: 1 }}):",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                if (receivedCards.isEmpty()) {
                                    Text("Ninguna carta recibida", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                } else {
                                    receivedCards.forEach { c ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CardImageThumbnail(
                                                model = c.miniatureUrl?.takeIf { it.isNotBlank() } ?: c.imageUrl,
                                                contentDescription = c.cardName,
                                                modifier = Modifier.size(36.dp, 52.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(c.cardName.orEmpty(), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("${c.set.orEmpty()} • ${c.condition.orEmpty()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                                Text("Cant: ${c.quantity ?: 1} | Acordado: $${c.agreedUponPrice ?: 0}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }

                        // Cartas que Entregas
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Cartas que entregas (${deliveredCards.sumOf { it.quantity ?: 1 }}):",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                if (deliveredCards.isEmpty()) {
                                    Text("Sin cartas entregadas (Compra pura)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                } else {
                                    deliveredCards.forEach { c ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CardImageThumbnail(
                                                model = c.miniatureUrl?.takeIf { it.isNotBlank() } ?: c.imageUrl,
                                                contentDescription = c.cardName,
                                                modifier = Modifier.size(36.dp, 52.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(c.cardName.orEmpty(), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("${c.set.orEmpty()} • ${c.condition.orEmpty()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                                Text("Cant: ${c.quantity ?: 1} | Acordado: $${c.agreedUponPrice ?: 0}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }

                        // Balance Monetario
                        val myDebt = details?.summary?.myDebt ?: 0
                        val counterpartDebt = details?.summary?.counterpartDebt ?: 0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val balanceText = when {
                                    myDebt > 0 -> "Debes pagar $$myDebt a @${details?.counterpart?.username ?: "Contraparte"}"
                                    counterpartDebt > 0 -> "@${details?.counterpart?.username ?: "Contraparte"} te debe $$counterpartDebt"
                                    else -> "Intercambio equitativo (sin diferencia de dinero)"
                                }
                                Text(
                                    text = balanceText,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.closePendingSummary() }) {
                Text("Cerrar")
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
