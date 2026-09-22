package com.proyectotitulo.appcomerciojcc.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.ProfileRepository
import com.proyectotitulo.appcomerciojcc.domain.models.ProfileUiState
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _uiState = MutableLiveData<ProfileUiState>(ProfileUiState.Loading)
    val uiState: LiveData<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val result = repository.getPrivateProfile()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = ProfileUiState.Success(response.data)
                } else {
                    _uiState.value = ProfileUiState.Error(
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = ProfileUiState.Error(
                    errors = listOf(error.message ?: "Error de red inesperado")
                )
            }
        }
    }
}