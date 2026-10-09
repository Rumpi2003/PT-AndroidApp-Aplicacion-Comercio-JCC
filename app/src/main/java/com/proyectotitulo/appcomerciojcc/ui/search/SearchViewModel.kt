package com.proyectotitulo.appcomerciojcc.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.InventoryRepository
import com.proyectotitulo.appcomerciojcc.data.repository.SearchRepository
import com.proyectotitulo.appcomerciojcc.domain.models.GetSetsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTcgsResponse.TCG
import com.proyectotitulo.appcomerciojcc.domain.models.SearchUiState
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = SearchRepository()
    private val inventoryRepository = InventoryRepository()

    private val _uiState = MutableLiveData<SearchUiState>(SearchUiState())
    val uiState: LiveData<SearchUiState> = _uiState

    init {
        loadTcgs()
    }

    private fun loadTcgs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(
                isLoadingTcgs = true,
                errors = null
            )

            val result = repository.getTcgs()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = _uiState.value?.copy(
                        tcgsList = response.data ?: emptyList(),
                        isLoadingTcgs = false
                    )
                } else {
                    _uiState.value = _uiState.value?.copy(
                        isLoadingTcgs = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value?.copy(
                    isLoadingTcgs = false,
                    errors = listOf(error.message ?: "Error de red inesperado")
                )
            }

        }
    }

    fun onTcgSelected(tcg: TCG) {
        _uiState.value = _uiState.value?.copy(
            selectedTcg = tcg,
            selectedSet = null,
            setsList = emptyList()
        )
        tcg.tcgId?.let { loadSets(it) }
    }

    fun loadSets(
        tcgId: String = _uiState.value?.selectedTcg?.tcgId.orEmpty(),
        sortBy: String = _uiState.value?.setSortBy ?: "release_date",
        sortOrder: String = _uiState.value?.setSortOrder ?: "desc"
    ) {
        if (tcgId.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(
                isLoadingSets = true,
                setSortBy = sortBy,
                setSortOrder = sortOrder,
                errors = null
            )

            val result = repository.getSets(tcgId, sortBy, sortOrder)
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = _uiState.value?.copy(
                        setsList = response.data?.results ?: emptyList(),
                        isLoadingSets = false
                    )
                } else {
                    _uiState.value = _uiState.value?.copy(
                        isLoadingSets = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value?.copy(
                    isLoadingSets = false,
                    errors = listOf(error.message ?: "Error al cargar los sets")
                )
            }
        }
    }

    fun onSetSelected(set: GetSetsResponse.Data.Set?) {
        _uiState.value = _uiState.value?.copy(selectedSet = set)
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value?.copy(query = newQuery)
    }

    fun toggleViewMode() {
        val currentMode = _uiState.value?.isGridView ?: true
        _uiState.value = _uiState.value?.copy(isGridView = !currentMode)
    }

    fun searchCards(isNewSearch: Boolean = true) {
        val currentState = _uiState.value ?: return
        val targetPage = if (isNewSearch) 1 else (currentState.currentPage + 1)

        viewModelScope.launch {
            if (isNewSearch) {
                _uiState.value = currentState.copy(
                    isSearchingCards = true,
                    cardsList = emptyList(),
                    currentPage = 1,
                    errors = null
                )
            } else {
                _uiState.value = currentState.copy(
                    isLoadingNextPage = true,
                    errors = null
                )
            }

            val result = repository.getCards(
                tcgId = currentState.selectedTcg?.tcgId.orEmpty(),
                setId = currentState.selectedSet?.setId.orEmpty(),
                name = currentState.query,
                sortBy = currentState.cardSortBy,
                sortOrder = currentState.carSortOrder,
                page = targetPage,
                limit = currentState.limiterPerPage
            )

            result.onSuccess { response ->
                if (response.status == "success") {
                    val newCards = response.data?.results ?: emptyList()

                    val updatedCardsList = if (isNewSearch) newCards else (currentState.cardsList + newCards)

                    _uiState.value = _uiState.value?.copy(
                        cardsList = updatedCardsList,
                        currentPage = response.data?.page ?: targetPage,
                        totalCards = response.data?.total ?: 0,
                        isSearchingCards = false,
                        isLoadingNextPage = false
                    )
                } else {
                    _uiState.value = _uiState.value?.copy(
                        isSearchingCards = false,
                        isLoadingNextPage = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value?.copy(
                    isSearchingCards = false,
                    isLoadingNextPage = false,
                    errors = listOf(error.message ?: "Error al buscar cartas")
                )
            }
        }
    }

    fun addInventoryCard(
        cardId: Int,
        condition: String,
        language: String,
        price: Int,
        quantity: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = inventoryRepository.addInventoryCard(
                cardId = cardId,
                condition = condition,
                language = language,
                price = price,
                quantity = quantity
            )
            result.onSuccess { response ->
                if (response.status == "success") {
                    onResult(true, response.message)
                } else {
                    val errorMsg = response.errors?.firstOrNull() ?: response.message
                    onResult(false, errorMsg)
                }
            }.onFailure { error ->
                onResult(false, error.message ?: "Error al añadir la carta al inventario")
            }
        }
    }

    fun addWishlistCard(
        cardId: Int,
        minimumCondition: String,
        language: String,
        quantity: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = inventoryRepository.addWishlistCard(
                cardId = cardId,
                minimumCondition = minimumCondition,
                language = language,
                quantity = quantity
            )
            result.onSuccess { response ->
                if (response.status == "success") {
                    onResult(true, response.message)
                } else {
                    val errorMsg = response.errors?.firstOrNull() ?: response.message
                    onResult(false, errorMsg)
                }
            }.onFailure { error ->
                onResult(false, error.message ?: "Error al añadir la carta a la lista de deseos")
            }
        }
    }
}