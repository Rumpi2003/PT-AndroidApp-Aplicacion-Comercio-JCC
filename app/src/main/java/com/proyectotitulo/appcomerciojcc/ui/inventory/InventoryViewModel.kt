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

    fun loadInventory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoadingInventory = true, errors = null)

            val result = repository.getInventory()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = _uiState.value?.copy(
                        inventoryCards = response.data ?: emptyList(),
                        isLoadingInventory = false
                    )
                } else {
                    _uiState.value = _uiState.value?.copy(
                        isLoadingInventory = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value?.copy(
                    isLoadingInventory = false,
                    errors = listOf(error.message ?: "Error al cargar el inventario")
                )
            }
        }
    }

    fun loadWishlist() {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoadingWishlist = true, errors = null)

            val result = repository.getWishlist()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = _uiState.value?.copy(
                        wishlistCards = response.data ?: emptyList(),
                        isLoadingWishlist = false
                    )
                } else {
                    _uiState.value = _uiState.value?.copy(
                        isLoadingWishlist = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value?.copy(
                    isLoadingWishlist = false,
                    errors = listOf(error.message ?: "Error al cargar la lista de deseos")
                )
            }
        }
    }
}