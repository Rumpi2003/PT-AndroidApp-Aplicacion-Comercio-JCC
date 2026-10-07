package com.proyectotitulo.appcomerciojcc.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.SearchRepository
import com.proyectotitulo.appcomerciojcc.domain.models.GetSetsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTcgsResponse.TCG
import com.proyectotitulo.appcomerciojcc.domain.models.SearchUiState
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = SearchRepository()

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
}