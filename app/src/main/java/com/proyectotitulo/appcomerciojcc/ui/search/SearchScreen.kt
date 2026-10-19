package com.proyectotitulo.appcomerciojcc.ui.search

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.proyectotitulo.appcomerciojcc.domain.models.GetCardsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetSetsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTcgsResponse.TCG
import com.proyectotitulo.appcomerciojcc.domain.models.SearchUiState
import com.proyectotitulo.appcomerciojcc.ui.components.CardImageThumbnail
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel
) {

    val uiState by viewModel.uiState.observeAsState(initial = SearchUiState())
    var showSetBottomSheet by remember { mutableStateOf(false) }
    var selectedCardForDetails by remember { mutableStateOf<GetCardsResponse.Data.Card?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (uiState.isLoadingTcgs) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Search(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                viewModel = viewModel,
                onOpenSetSheet = { showSetBottomSheet = true },
                onCardClick = { card ->
                    selectedCardForDetails = card
                }
            )

            if (showSetBottomSheet && uiState.selectedTcg != null) {
                SetSelectionBottomSheet(
                    setsList = uiState.setsList,
                    selectedSet = uiState.selectedSet,
                    currentSortBy = uiState.setSortBy,
                    currentSortOrder = uiState.setSortOrder,
                    isLoadingSets = uiState.isLoadingSets,
                    onDismiss = { showSetBottomSheet = false },
                    onSetSelected = { set ->
                        viewModel.onSetSelected(set)
                        showSetBottomSheet = false
                    },
                    onApplySort = { sortBy, sortOrder ->
                        viewModel.loadSets(
                            tcgId = uiState.selectedTcg?.tcgId.orEmpty(),
                            sortBy = sortBy,
                            sortOrder = sortOrder
                        )
                    }
                )
            }

            selectedCardForDetails?.let { card ->
                AddCardOptionsDialog(
                    card = card,
                    onDismissRequest = { selectedCardForDetails = null },
                    onAddInventory = { cardId, condition, language, price, quantity, onResult ->
                        viewModel.addInventoryCard(cardId, condition, language, price, quantity) { success, msg ->
                            onResult(success, msg)
                            if (success) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msg ?: "Carta añadida al inventario con éxito"
                                    )
                                }
                            }
                        }
                    },
                    onAddWishlist = { cardId, minCondition, language, quantity, onResult ->
                        viewModel.addWishlistCard(cardId, minCondition, language, quantity) { success, msg ->
                            onResult(success, msg)
                            if (success) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msg ?: "Carta añadida a la lista de deseos con éxito"
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun Search(
    modifier: Modifier,
    uiState: SearchUiState,
    viewModel: SearchViewModel,
    onOpenSetSheet: () -> Unit,
    onCardClick: (GetCardsResponse.Data.Card) -> Unit
) {
    Column(
        modifier = modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(
            query = uiState.query,
            onQueryChanged = { viewModel.onQueryChanged(it) },
            onSearchSelected = { viewModel.searchCards(isNewSearch = true) },
            enabled = !uiState.isSearchingCards
        )
        SearchButton(
            onClick = { viewModel.searchCards(isNewSearch = true) },
            isSearching = uiState.isSearchingCards,
        )
        TcgDropdown(
            tcgsList = uiState.tcgsList,
            selectedTcg = uiState.selectedTcg,
            onTcgSelected = { viewModel.onTcgSelected(it) },
            enabled = !uiState.isLoadingTcgs
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            FilterChip(
                selected = uiState.selectedSet != null,
                onClick = onOpenSetSheet,
                label = {
                    Text(
                        text = uiState.selectedSet?.setName ?: "Seleccionar Set / Expansión"
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = "Icono Set"
                    )
                },
                enabled = uiState.selectedTcg != null && !uiState.isLoadingTcgs
            )
        }

        if (uiState.totalCards > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mostrando ${uiState.cardsList.size} de ${uiState.totalCards} cartas encontradas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                IconButton(onClick = { viewModel.toggleViewMode() }) {
                    Icon(
                        imageVector = if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Alternar vista",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (uiState.isGridView) {
            CardsGrid(
                cardsList = uiState.cardsList,
                totalCards = uiState.totalCards,
                isLoadingNextPage = uiState.isLoadingNextPage,
                onSearchSelected = { viewModel.searchCards(isNewSearch = false) },
                onCardClick = onCardClick
            )
        } else {
            CardList(
                cardsList = uiState.cardsList,
                totalCards = uiState.totalCards,
                isLoadingNextPage = uiState.isLoadingNextPage,
                onSearchSelected = { viewModel.searchCards(isNewSearch = false) },
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchSelected: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
){
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Buscar carta por nombre...") },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChanged("") },
                    enabled = enabled
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchSelected() })
    )

}

@Composable
fun SearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSearching: Boolean
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = !isSearching,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
    ) {
        if (isSearching) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else {
            Text("Buscar Cartas")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcgDropdown(
    tcgsList: List<TCG>?,
    selectedTcg: TCG?,
    onTcgSelected: (TCG) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedTcg?.tcgName ?: "Seleccionar juego",
            onValueChange = {},
            readOnly = true,
            label = { Text("Juego (TCG)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Icono TCG"
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
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            tcgsList?.forEach { tcg ->
                DropdownMenuItem(
                    text = { Text(text = tcg.tcgName.orEmpty()) },
                    onClick = {
                        onTcgSelected(tcg)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetSelectionBottomSheet(
    setsList: List<GetSetsResponse.Data.Set>,
    selectedSet: GetSetsResponse.Data.Set?,
    currentSortBy: String,
    currentSortOrder: String,
    isLoadingSets: Boolean,
    onDismiss: () -> Unit,
    onSetSelected: (GetSetsResponse.Data.Set?) -> Unit,
    onApplySort: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var tempSortBy by remember { mutableStateOf(currentSortBy) }
    var tempSortOrder by remember { mutableStateOf(currentSortOrder) }
    var showSortPanel by remember { mutableStateOf(false) }

    val filteredSets = remember(setsList, searchQuery) {
        if (searchQuery.isBlank()) setsList
        else setsList.filter { it.setName.orEmpty().contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seleccionar Expansión (Set)",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { showSortPanel = !showSortPanel }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Ordenar Sets",
                        tint = if (showSortPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (showSortPanel) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Ordenar por:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = tempSortBy == "release_date",
                                onClick = { tempSortBy = "release_date" },
                                label = { Text("Fecha Lanzamiento") }
                            )
                            FilterChip(
                                selected = tempSortBy == "name",
                                onClick = { tempSortBy = "name" },
                                label = { Text("Nombre") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Clasificación:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = tempSortOrder == "desc",
                                onClick = { tempSortOrder = "desc" },
                                label = { Text("Descendente") }
                            )
                            FilterChip(
                                selected = tempSortOrder == "asc",
                                onClick = { tempSortOrder = "asc" },
                                label = { Text("Ascendente") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                onApplySort(tempSortBy, tempSortOrder)
                                showSortPanel = false
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("Ordenar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar set por nombre...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoadingSets) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetSelected(null) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Todos los Sets",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (selectedSet == null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        HorizontalDivider()
                    }

                    items(filteredSets) { set ->
                        val isSelected = selectedSet?.setId == set.setId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetSelected(set) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = set.setName.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun CardItemRow(
    card: GetCardsResponse.Data.Card,
    onClick: () -> Unit = {}
) {
    val imageModel = card.miniatureUrl.takeIf { it.isNotBlank() } ?: card.imageUrl

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
                    .padding(vertical = 10.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = card.cardName.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column{
                    Text(
                        text = card.set.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = card.rarity.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun CardGridItem(
    card: GetCardsResponse.Data.Card,
    onClick: () -> Unit = {}
) {
    val imageModel = card.miniatureUrl.takeIf { it.isNotBlank() } ?: card.imageUrl

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
                    .height(150.dp),
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = card.set.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = card.rarity.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CardList(
    cardsList: List<GetCardsResponse.Data.Card>,
    totalCards: Int,
    isLoadingNextPage: Boolean,
    onSearchSelected: () -> Unit,
    onCardClick: (GetCardsResponse.Data.Card) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cardsList) { card ->
            CardItemRow(
                card = card,
                onClick = { onCardClick(card) }
            )
        }

        if (cardsList.size < totalCards) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingNextPage) {
                        CircularProgressIndicator()
                    } else {
                        OutlinedButton(
                            onClick = onSearchSelected
                        ) {
                            Text("Cargar más cartas")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardsGrid(
    cardsList: List<GetCardsResponse.Data.Card>,
    totalCards: Int,
    isLoadingNextPage: Boolean,
    onSearchSelected: () -> Unit,
    onCardClick: (GetCardsResponse.Data.Card) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cardsList) { card ->
            CardGridItem(
                card = card,
                onClick = { onCardClick(card) }
            )
        }

        if (cardsList.size < totalCards) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingNextPage) {
                        CircularProgressIndicator()
                    } else {
                        OutlinedButton(onClick = onSearchSelected) {
                            Text("Cargar más cartas")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardOptionsDialog(
    card: GetCardsResponse.Data.Card,
    onDismissRequest: () -> Unit,
    onAddInventory: (Int, String, String, Int, Int, (Boolean, String?) -> Unit) -> Unit,
    onAddWishlist: (Int, String, String, Int, (Boolean, String?) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Inventario, 1 = Lista de Deseos
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

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

    var selectedCondition by remember { mutableStateOf(conditions[0]) }
    var selectedLanguage by remember { mutableStateOf(languages[0]) }
    var priceText by remember { mutableStateOf("") }
    var inventoryQuantityText by remember { mutableStateOf("1") }

    var selectedMinCondition by remember { mutableStateOf(conditions[0]) }
    var selectedWishlistLanguage by remember { mutableStateOf(languages[0]) }
    var wishlistQuantityText by remember { mutableStateOf("1") }

    var expandedConditionDropdown by remember { mutableStateOf(false) }
    var expandedLanguageDropdown by remember { mutableStateOf(false) }

    val dialogImageModel = card.miniatureUrl.takeIf { it.isNotBlank() } ?: card.imageUrl

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        title = {
            Text(
                text = "Añadir Carta",
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
                            model = dialogImageModel,
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
                                text = "${card.set.orEmpty()} • ${card.rarity.orEmpty()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                            successMessage = null
                        },
                        text = { Text("Inventario") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                            successMessage = null
                        },
                        text = { Text("Lista Deseos") }
                    )
                }

                if (selectedTab == 0) {
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
                            conditions.forEach { condition ->
                                DropdownMenuItem(
                                    text = { Text(condition) },
                                    onClick = {
                                        selectedCondition = condition
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
                        label = { Text("Precio en que se ofrece ($)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    OutlinedTextField(
                        value = inventoryQuantityText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 3) {
                                inventoryQuantityText = input
                            }
                        },
                        label = { Text("Cantidad de copias") },
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                } else {
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
                            conditions.forEach { condition ->
                                DropdownMenuItem(
                                    text = { Text(condition) },
                                    onClick = {
                                        selectedMinCondition = condition
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
                            value = selectedWishlistLanguage,
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
                                        selectedWishlistLanguage = lang
                                        expandedLanguageDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = wishlistQuantityText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 3) {
                                wishlistQuantityText = input
                            }
                        },
                        label = { Text("Cantidad deseada") },
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                }

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                successMessage?.let { success ->
                    Text(
                        text = success,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cardId = card.cardId ?: 0
                    if (cardId == 0) {
                        errorMessage = "ID de carta no válido"
                        return@Button
                    }

                    if (selectedTab == 0) {
                        val price = priceText.toIntOrNull() ?: -1
                        val qty = inventoryQuantityText.toIntOrNull() ?: 0
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
                        successMessage = null
                        onAddInventory(cardId, selectedCondition, selectedLanguage, price, qty) { success, msg ->
                            isLoading = false
                            if (success) {
                                successMessage = msg ?: "Añadida al inventario con éxito"
                                onDismissRequest()
                            } else {
                                errorMessage = msg ?: "Error al añadir al inventario"
                            }
                        }
                    } else {
                        val qty = wishlistQuantityText.toIntOrNull() ?: 0
                        if (qty <= 0) {
                            errorMessage = "Ingresa una cantidad mayor a 0"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        onAddWishlist(cardId, selectedMinCondition, selectedWishlistLanguage, qty) { success, msg ->
                            isLoading = false
                            if (success) {
                                successMessage = msg ?: "Añadida a la lista de deseos con éxito"
                                onDismissRequest()
                            } else {
                                errorMessage = msg ?: "Error al añadir a la lista de deseos"
                            }
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
                    Text(if (selectedTab == 0) "Añadir a Inventario" else "Añadir a Deseos")
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
