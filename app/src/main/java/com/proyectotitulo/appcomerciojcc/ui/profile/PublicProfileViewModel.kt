package com.proyectotitulo.appcomerciojcc.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.ProfileRepository
import com.proyectotitulo.appcomerciojcc.domain.models.GetPublicProfileResponse
import kotlinx.coroutines.launch

sealed interface PublicProfileUiState {
    data object Loading : PublicProfileUiState
    data class Success(val profile: GetPublicProfileResponse.Profile) : PublicProfileUiState
    data class Error(val errors: List<String>?) : PublicProfileUiState
}

class PublicProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _uiState = MutableLiveData<PublicProfileUiState>(PublicProfileUiState.Loading)
    val uiState: LiveData<PublicProfileUiState> = _uiState

    fun loadPublicProfile(userId: Int) {
        viewModelScope.launch {
            _uiState.value = PublicProfileUiState.Loading

            val result = repository.getPublicProfile(userId)
            result.onSuccess { response ->
                if (response.status == "success" && response.data != null) {
                    _uiState.value = PublicProfileUiState.Success(response.data)
                } else {
                    _uiState.value = PublicProfileUiState.Error(response.errors ?: listOf(response.message))
                }
            }.onFailure { error ->
                _uiState.value = PublicProfileUiState.Error(listOf(error.message ?: "Error al cargar el perfil público"))
            }
        }
    }
}
