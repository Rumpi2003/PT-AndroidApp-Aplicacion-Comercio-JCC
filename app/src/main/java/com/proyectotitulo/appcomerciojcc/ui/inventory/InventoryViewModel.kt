package com.proyectotitulo.appcomerciojcc.ui.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.InventoryRepository
import com.proyectotitulo.appcomerciojcc.domain.models.InventoryUiState
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {

    private val repository = InventoryRepository()

    private val _uiState = MutableLiveData<InventoryUiState>(InventoryUiState())
    val uiState: LiveData<InventoryUiState> = _uiState


    init {
        loadInventory()
        loadWishlist()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value?.copy(selectedTab = tabIndex)
        if (tabIndex == 0) {
            loadInventory()
        } else {
            loadWishlist()
        }
    }

    fun toggleViewMode() {
        val currentMode = _uiState.value?.isGridView ?: true
        _uiState.value = _uiState.value?.copy(isGridView = !currentMode)
    }

    fun loadInventory(forceRefresh: Boolean = false, sortBy: String? = "nombre_carta", sortOrder: String? = "ASC") {
        val currentState = _uiState.value ?: InventoryUiState()
        if (!forceRefresh && currentState.inventoryCards.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.value = currentState.copy(isRefreshing = true, errors = null)
            } else {
                _uiState.value = currentState.copy(isLoadingInventory = true, errors = null)
            }
            val result = repository.getInventory(sortBy = sortBy, sortOrder = sortOrder)
            val latestState = _uiState.value ?: InventoryUiState()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latestState.copy(
                        inventoryCards = response.data ?: emptyList(),
                        isLoadingInventory = false,
                        isRefreshing = false
                    )
                } else {
                    _uiState.value = latestState.copy(
                        isLoadingInventory = false,
                        isRefreshing = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = latestState.copy(
                    isLoadingInventory = false,
                    isRefreshing = false,
                    errors = listOf(error.message ?: "Error al cargar el inventario")
                )
            }
        }
    }

    fun loadWishlist(forceRefresh: Boolean = false, sortBy: String? = "nombre_carta", sortOrder: String? = "ASC") {
        val currentState = _uiState.value ?: InventoryUiState()
        if (!forceRefresh && currentState.wishlistCards.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.value = currentState.copy(isRefreshing = true, errors = null)
            } else {
                _uiState.value = currentState.copy(isLoadingWishlist = true, errors = null)
            }
            val result = repository.getWishlist(sortBy = sortBy, sortOrder = sortOrder)
            val latestState = _uiState.value ?: InventoryUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latestState.copy(
                        wishlistCards = response.data ?: emptyList(),
                        isLoadingWishlist = false,
                        isRefreshing = false
                    )
                } else {
                    _uiState.value = latestState.copy(
                        isLoadingWishlist = false,
                        isRefreshing = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = latestState.copy(
                    isLoadingWishlist = false,
                    isRefreshing = false,
                    errors = listOf(error.message ?: "Error al cargar la lista de deseos")
                )
            }
        }
    }

    fun onSortOptionSelected(sortBy: String, sortOrder: String) {
        val currentState = _uiState.value ?: InventoryUiState()
        if (currentState.selectedTab == 0) {
            loadInventory(forceRefresh = true, sortBy = sortBy, sortOrder = sortOrder)
        } else {
            loadWishlist(forceRefresh = true, sortBy = sortBy, sortOrder = sortOrder)
        }
    }
}