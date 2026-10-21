package com.proyectotitulo.appcomerciojcc.ui.inventory

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.proyectotitulo.appcomerciojcc.domain.models.GetInventoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetWishlistResponse
import com.proyectotitulo.appcomerciojcc.domain.models.InventoryUiState
import com.proyectotitulo.appcomerciojcc.domain.models.SearchUiState
import com.proyectotitulo.appcomerciojcc.ui.components.CardImageThumbnail
import com.proyectotitulo.appcomerciojcc.ui.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel
) {
    val uiState by viewModel.uiState.observeAsState(initial = InventoryUiState())

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                if (uiState.selectedTab == 0) viewModel.loadInventory(forceRefresh = true)
                else viewModel.loadWishlist(forceRefresh = true)
            },
            modifier = modifier.fillMaxSize()
        ) {
            Inventory(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun Inventory(
    modifier: Modifier,
    uiState: InventoryUiState,
    viewModel: InventoryViewModel
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
                        inventoryCards = uiState.inventoryCards
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
                        wishlistCards = uiState.wishlistCards
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
                InventoryCardGridItem(card = card)
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
                InventoryCardRowItem(card = card)
            }
        }
    }
}

@Composable
fun WishlistCards(
    isGridView: Boolean,
    wishlistCards: List<GetWishlistResponse.WishlistCard>,
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
                WishlistCardGridItem(card = card)
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
                WishlistCardRowItem(card = card)
            }
        }
    }
}
@Composable
fun InventoryCardGridItem(card: GetInventoryResponse.InventoryCard) {
    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    Card(
        modifier = Modifier.fillMaxWidth(),
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
fun InventoryCardRowItem(card: GetInventoryResponse.InventoryCard) {
    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    Card(
        modifier = Modifier.fillMaxWidth(),
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
fun WishlistCardGridItem(card: GetWishlistResponse.WishlistCard) {
    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    Card(
        modifier = Modifier.fillMaxWidth(),
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
fun WishlistCardRowItem(card: GetWishlistResponse.WishlistCard) {
    val imageModel = card.miniatureUrl?.takeIf { it.isNotBlank() } ?: card.imageUrl

    Card(
        modifier = Modifier.fillMaxWidth(),
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
