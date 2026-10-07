package com.proyectotitulo.appcomerciojcc.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.proyectotitulo.appcomerciojcc.domain.models.GetSetsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTcgsResponse.TCG
import com.proyectotitulo.appcomerciojcc.domain.models.SearchUiState

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel
) {

    val uiState by viewModel.uiState.observeAsState(initial = SearchUiState())
    var showSetBottomSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            vArrangement = Arrangement.spacedBy(12.dp),
            hAlignment = Alignment.CenterHorizontally,
            uiState = uiState,
            viewModel = viewModel,
            onOpenSetSheet = { showSetBottomSheet = true }
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
    }
}

@Composable
fun Search(
    modifier: Modifier,
    vArrangement: Arrangement.Vertical,
    hAlignment: Alignment.Horizontal,
    uiState: SearchUiState,
    viewModel: SearchViewModel,
    onOpenSetSheet: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = vArrangement,
        horizontalAlignment = hAlignment
    ) {
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
    }
}

@Composable
fun SearchBar(){

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
