package com.proyectotitulo.appcomerciojcc.ui.inventory

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.proyectotitulo.appcomerciojcc.domain.models.GetInventoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetWishlistResponse
import com.proyectotitulo.appcomerciojcc.domain.models.InventoryUiState
import com.proyectotitulo.appcomerciojcc.ui.components.CardImageThumbnail
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel
) {
    val uiState by viewModel.uiState.observeAsState(initial = InventoryUiState())

    var selectedInventoryCard by remember { mutableStateOf<GetInventoryResponse.InventoryCard?>(null) }
    var selectedWishlistCard by remember { mutableStateOf<GetWishlistResponse.WishlistCard?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = {
                    if (uiState.selectedTab == 0) viewModel.loadInventory(forceRefresh = true)
                    else viewModel.loadWishlist(forceRefresh = true)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Inventory(
                    modifier = Modifier.fillMaxSize(),
                    uiState = uiState,
                    viewModel = viewModel,
                    onInventoryCardClick = { card ->
                        selectedInventoryCard = card
                    },
                    onWishlistCardClick = { card ->
                        selectedWishlistCard = card
                    }
                )
            }

            selectedInventoryCard?.let { card ->
                val cardId = card.inventoryCardId ?: 0
                EditInventoryCardDialog(
                    card = card,
                    onDismissRequest = { selectedInventoryCard = null },
                    onConfirmUpdate = { condition, language, price, quantity, onResult ->
                        viewModel.patchInventoryCard(cardId, condition, language, price, quantity) { success, msg ->
                            onResult(success, msg)
                            if (success) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msg ?: "Carta actualizada con éxito"
                                    )
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        showDeleteConfirmDialog = true
                    }
                )

                if (showDeleteConfirmDialog) {
                    ConfirmDeleteCardDialog(
                        cardName = card.cardName.orEmpty(),
                        onDismissRequest = { showDeleteConfirmDialog = false },
                        onConfirmDelete = { onResult ->
                            viewModel.deleteInventoryCard(cardId) { success, msg ->
                                onResult(success, msg)
                                if (success) {
                                    showDeleteConfirmDialog = false
                                    selectedInventoryCard = null
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = msg ?: "Carta eliminada del inventario con éxito"
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            selectedWishlistCard?.let { card ->
                val cardId = card.wishlistCardId ?: 0
                EditWishlistCardDialog(
                    card = card,
                    onDismissRequest = { selectedWishlistCard = null },
                    onConfirmUpdate = { minCondition, language, quantity, onResult ->
                        viewModel.patchWishlistCard(cardId, minCondition, language, quantity) { success, msg ->
                            onResult(success, msg)
                            if (success) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msg ?: "Carta de deseos actualizada con éxito"
                                    )
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        showDeleteConfirmDialog = true
                    }
                )

                if (showDeleteConfirmDialog) {
                    ConfirmDeleteCardDialog(
                        cardName = card.cardName.orEmpty(),
                        onDismissRequest = { showDeleteConfirmDialog = false },
                        onConfirmDelete = { onResult ->
                            viewModel.deleteWishlistCard(cardId) { success, msg ->
                                onResult(success, msg)
                                if (success) {
                                    showDeleteConfirmDialog = false
                                    selectedWishlistCard = null
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = msg ?: "Carta eliminada de la lista de deseos con éxito"
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Inventory(
    modifier: Modifier,
    uiState: InventoryUiState,
    viewModel: InventoryViewModel,
    onInventoryCardClick: (GetInventoryResponse.InventoryCard) -> Unit = {},
    onWishlistCardClick: (GetWishlistResponse.WishlistCard) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabSelection(
            selectedTab = uiState.selectedTab,
            selectTab = { viewModel.selectTab(it) }
        )

        val currentListSize = if (uiState.selectedTab == 0) uiState.inventoryCards.size else uiState.wishlistCards.size
        val currentSortBy = if (uiState.selectedTab == 0) uiState.inventorySortBy else uiState.wishlistSortBy
        val currentSortOrder = if (uiState.selectedTab == 0) uiState.inventorySortOrder else uiState.wishlistSortOrder

        TotalAndViewModeSelection(
            selectedTab = uiState.selectedTab,
            currentListSize = currentListSize,
            currentSortBy = currentSortBy,
            currentSortOrder = currentSortOrder,
            onSortSelected = { sortBy, sortOrder ->
                viewModel.onSortOptionSelected(sortBy, sortOrder)
            },
            toggleViewMode = { viewModel.toggleViewMode() },
            isGridView = uiState.isGridView
        )

        val isLoading = if (uiState.selectedTab == 0) uiState.isLoadingInventory else uiState.isLoadingWishlist
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errors != null) {
            ErrorScreen(
                errors = uiState.errors,
                selectedTab = uiState.selectedTab,
                loadInventory = { viewModel.loadInventory() },
                loadWishlist = { viewModel.loadWishlist() }
            )
        } else {
            if (uiState.selectedTab == 0) {
                if (uiState.inventoryCards.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes cartas en tu inventario.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    InventoryCards(
                        isGridView = uiState.isGridView,
                        inventoryCards = uiState.inventoryCards,
                        onCardClick = onInventoryCardClick
                    )
                }
            } else {
                if (uiState.wishlistCards.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes cartas en tu lista de deseos.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    WishlistCards(
                        isGridView = uiState.isGridView,
                        wishlistCards = uiState.wishlistCards,
                        onCardClick = onWishlistCardClick
                    )
                }
            }
        }
    }
}

@Composable
fun TabSelection(
    selectedTab: Int,
    selectTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTab,
        modifier = modifier) {
        Tab(
            selected = selectedTab == 0,
            onClick = { selectTab(0) },
            text = { Text("Mi Inventario") }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { selectTab(1) },
            text = { Text("Lista de Deseos") }
        )
    }
}

data class SortOption(
    val label: String,
    val sortBy: String,
    val sortOrder: String
)

@Composable
fun TotalAndViewModeSelection(
    selectedTab: Int,
    currentListSize: Int,
    currentSortBy: String,
    currentSortOrder: String,
    onSortSelected: (String, String) -> Unit,
    toggleViewMode: () -> Unit,
    isGridView: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val options = if (selectedTab == 0) {
        listOf(
            SortOption("Nombre ▲", "nombre_carta", "ASC"),
            SortOption("Nombre ▼", "nombre_carta", "DESC"),
            SortOption("Precio ▲", "precio", "ASC"),
            SortOption("Precio ▼", "precio", "DESC"),
            SortOption("Franquicia ▲", "franquicia", "ASC"),
            SortOption("Franquicia ▼", "franquicia", "DESC")
        )
    } else {
        listOf(
            SortOption("Nombre ▲", "nombre_carta", "ASC"),
            SortOption("Nombre ▼", "nombre_carta", "DESC"),
            SortOption("Franquicia ▲", "franquicia", "ASC"),
            SortOption("Franquicia ▼", "franquicia", "DESC")
        )
    }

    val selectedOption = options.find { it.sortBy == currentSortBy && it.sortOrder == currentSortOrder }
        ?: options.first()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (selectedTab == 0) "Total ofrecidas: $currentListSize" else "Total buscadas: $currentListSize",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                FilterChip(
                    selected = true,
                    onClick = { expanded = true },
                    label = {
                        Text(
                            text = selectedOption.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Desplegar opciones de orden"
                        )
                    }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option.label) },
                            onClick = {
                                onSortSelected(option.sortBy, option.sortOrder)
                                expanded = false
                            }
                        )
                    }
                }
            }

            IconButton(onClick = toggleViewMode) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Alternar vista",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(
    errors: List<String>?,
    selectedTab: Int,
    loadInventory: () -> Unit,
    loadWishlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
            text = "Error al cargar la lista",
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
            onClick = {
                if (selectedTab == 0) loadInventory()
                else loadWishlist()
            }
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
fun InventoryCards(
    isGridView: Boolean,
    inventoryCards: List<GetInventoryResponse.InventoryCard>,
    onCardClick: (GetInventoryResponse.InventoryCard) -> Unit = {},
    gridModifier: Modifier = Modifier,
    listModifier: Modifier = Modifier
) {
    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = gridModifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(inventoryCards) { card ->
                InventoryCardGridItem(
                    card = card,
                    onClick = { onCardClick(card) }
                )
            }
        }
    } else {
        LazyColumn(
            modifier = listModifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(inventoryCards) { card ->
                InventoryCardRowItem(
                    card = card,
                    onClick = { onCardClick(card) }
                )
            }
        }
    }
}

@Composable
fun WishlistCards(
    isGridView: Boolean,
    wishlistCards: List<GetWishlistResponse.WishlistCard>,
    onCardClick: (GetWishlistResponse.WishlistCard) -> Unit = {},
    gridModifier: Modifier = Modifier,
    listModifier: Modifier = Modifier
) {
    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = gridModifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(wishlistCards) { card ->
                WishlistCardGridItem(
                    card = card,
                    onClick = { onCardClick(card) }
                )
            }
        }
    } else {
        LazyColumn(
            modifier = listModifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(wishlistCards) { card ->
                WishlistCardRowItem(
                    card = card,
                    onClick = { onCardClick(card) }
                )
            }
        }
    }
}

@Composable
fun InventoryCardGridItem(
    card: GetInventoryResponse.InventoryCard,
    onClick: () -> Unit = {}
) {
    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CardImageThumbnail(
                    model = imageModel,
                    contentDescription = card.cardName,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = card.cardName.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${card.tcg.orEmpty()} • ${card.set.orEmpty()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Rareza: ${card.cardRarity.orEmpty()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Estado: ${card.condition.orEmpty()} (${card.language.orEmpty()})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Copias: ${card.quantity ?: 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${card.price ?: 0}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun InventoryCardRowItem(
    card: GetInventoryResponse.InventoryCard,
    onClick: () -> Unit = {}
) {
    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(110.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CardImageThumbnail(
                model = imageModel,
                contentDescription = card.cardName,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(70.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = card.cardName.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${card.tcg.orEmpty()} • ${card.set.orEmpty()} • ${card.cardRarity.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estado: ${card.condition.orEmpty()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Idioma: ${card.language.orEmpty()} • Copias: ${card.quantity ?: 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Text(
                        text = "$${card.price ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun WishlistCardGridItem(
    card: GetWishlistResponse.WishlistCard,
    onClick: () -> Unit = {}
) {
    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CardImageThumbnail(
                    model = imageModel,
                    contentDescription = card.cardName,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = card.cardName.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${card.tcg.orEmpty()} • ${card.set.orEmpty()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Rareza: ${card.cardRarity.orEmpty()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Mín. Estado: ${card.minimumCondition.orEmpty()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Idioma: ${card.language.orEmpty()} • Buscadas: ${card.quantity ?: 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WishlistCardRowItem(
    card: GetWishlistResponse.WishlistCard,
    onClick: () -> Unit = {}
) {
    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(110.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CardImageThumbnail(
                model = imageModel,
                contentDescription = card.cardName,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(70.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = card.cardName.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${card.tcg.orEmpty()} • ${card.set.orEmpty()} • ${card.cardRarity.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column {
                    Text(
                        text = "Estado Mínimo: ${card.minimumCondition.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Idioma: ${card.language.orEmpty()} • Buscadas: ${card.quantity ?: 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInventoryCardDialog(
    card: GetInventoryResponse.InventoryCard,
    onDismissRequest: () -> Unit,
    onConfirmUpdate: (String, String, Int, Int, (Boolean, String?) -> Unit) -> Unit,
    onDeleteClick: () -> Unit
) {
    val conditions = remember {
        listOf(
            "PERFECTA",
            "RECIEN_ABIERTA",
            "DESGASTE_MENOR",
            "DESGASTE_VISIBLE",
            "DESGASTE_SEVERO",
            "DAÑADA",
            "DESTRUIDA"
        )
    }

    val languages = remember {
        listOf(
            "Inglés",
            "Japonés",
            "Español",
            "Francés",
            "Alemán",
            "Italiano",
            "Portugués",
            "Coreano",
            "Chino"
        )
    }

    var selectedCondition by remember { mutableStateOf(card.condition ?: conditions[0]) }
    var selectedLanguage by remember { mutableStateOf(card.language ?: languages[0]) }
    var priceText by remember { mutableStateOf(card.price?.toString() ?: "0") }
    var quantityText by remember { mutableStateOf(card.quantity?.toString() ?: "1") }

    var expandedConditionDropdown by remember { mutableStateOf(false) }
    var expandedLanguageDropdown by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        title = {
            Text(
                text = "Editar Carta de Inventario",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardImageThumbnail(
                            model = imageModel,
                            contentDescription = card.cardName,
                            modifier = Modifier.size(50.dp, 70.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = card.cardName.orEmpty(),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${card.tcg.orEmpty()} • ${card.set.orEmpty()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedConditionDropdown,
                    onExpandedChange = { if (!isLoading) expandedConditionDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCondition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado de la carta") },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedConditionDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isLoading
                    )
                    ExposedDropdownMenu(
                        expanded = expandedConditionDropdown,
                        onDismissRequest = { expandedConditionDropdown = false }
                    ) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond) },
                                onClick = {
                                    selectedCondition = cond
                                    expandedConditionDropdown = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedLanguageDropdown,
                    onExpandedChange = { if (!isLoading) expandedLanguageDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLanguage,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Idioma") },
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguageDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isLoading
                    )
                    ExposedDropdownMenu(
                        expanded = expandedLanguageDropdown,
                        onDismissRequest = { expandedLanguageDropdown = false }
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    selectedLanguage = lang
                                    expandedLanguageDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            priceText = input
                        }
                    },
                    label = { Text("Precio ($)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 3) {
                            quantityText = input
                        }
                    },
                    label = { Text("Cantidad de copias") },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }

                Button(
                    onClick = {
                        val price = priceText.toIntOrNull() ?: -1
                        val qty = quantityText.toIntOrNull() ?: 0
                        if (price < 0) {
                            errorMessage = "Ingresa un precio válido"
                            return@Button
                        }
                        if (qty <= 0) {
                            errorMessage = "Ingresa una cantidad mayor a 0"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null
                        onConfirmUpdate(selectedCondition, selectedLanguage, price, qty) { success, msg ->
                            isLoading = false
                            if (success) {
                                onDismissRequest()
                            } else {
                                errorMessage = msg ?: "Error al actualizar la carta"
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
                        Text("Guardar")
                    }
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
fun EditWishlistCardDialog(
    card: GetWishlistResponse.WishlistCard,
    onDismissRequest: () -> Unit,
    onConfirmUpdate: (String, String, Int, (Boolean, String?) -> Unit) -> Unit,
    onDeleteClick: () -> Unit
) {
    val conditions = remember {
        listOf(
            "PERFECTA",
            "RECIEN_ABIERTA",
            "DESGASTE_MENOR",
            "DESGASTE_VISIBLE",
            "DESGASTE_SEVERO",
            "DAÑADA",
            "DESTRUIDA"
        )
    }

    val languages = remember {
        listOf(
            "Inglés",
            "Japonés",
            "Español",
            "Francés",
            "Alemán",
            "Italiano",
            "Portugués",
            "Coreano",
            "Chino"
        )
    }

    var selectedMinCondition by remember { mutableStateOf(card.minimumCondition ?: conditions[0]) }
    var selectedLanguage by remember { mutableStateOf(card.language ?: languages[0]) }
    var quantityText by remember { mutableStateOf(card.quantity?.toString() ?: "1") }

    var expandedConditionDropdown by remember { mutableStateOf(false) }
    var expandedLanguageDropdown by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        title = {
            Text(
                text = "Editar Carta de Lista de Deseos",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardImageThumbnail(
                            model = imageModel,
                            contentDescription = card.cardName,
                            modifier = Modifier.size(50.dp, 70.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = card.cardName.orEmpty(),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${card.tcg.orEmpty()} • ${card.set.orEmpty()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedConditionDropdown,
                    onExpandedChange = { if (!isLoading) expandedConditionDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedMinCondition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado mínimo aceptado") },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedConditionDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isLoading
                    )
                    ExposedDropdownMenu(
                        expanded = expandedConditionDropdown,
                        onDismissRequest = { expandedConditionDropdown = false }
                    ) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond) },
                                onClick = {
                                    selectedMinCondition = cond
                                    expandedConditionDropdown = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedLanguageDropdown,
                    onExpandedChange = { if (!isLoading) expandedLanguageDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLanguage,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Idioma") },
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguageDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isLoading
                    )
                    ExposedDropdownMenu(
                        expanded = expandedLanguageDropdown,
                        onDismissRequest = { expandedLanguageDropdown = false }
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    selectedLanguage = lang
                                    expandedLanguageDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 3) {
                            quantityText = input
                        }
                    },
                    label = { Text("Cantidad deseada") },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }

                Button(
                    onClick = {
                        val qty = quantityText.toIntOrNull() ?: 0
                        if (qty <= 0) {
                            errorMessage = "Ingresa una cantidad mayor a 0"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null
                        onConfirmUpdate(selectedMinCondition, selectedLanguage, qty) { success, msg ->
                            isLoading = false
                            if (success) {
                                onDismissRequest()
                            } else {
                                errorMessage = msg ?: "Error al actualizar la carta"
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
                        Text("Guardar")
                    }
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
fun ConfirmDeleteCardDialog(
    cardName: String,
    onDismissRequest: () -> Unit,
    onConfirmDelete: ((Boolean, String?) -> Unit) -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        title = {
            Text(
                text = "Eliminar Carta",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = "¿Estás seguro de que deseas eliminar \"$cardName\"? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
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
                    onConfirmDelete { success, msg ->
                        isLoading = false
                        if (success) {
                            onDismissRequest()
                        } else {
                            errorMessage = msg ?: "Error al eliminar la carta"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Eliminar")
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
