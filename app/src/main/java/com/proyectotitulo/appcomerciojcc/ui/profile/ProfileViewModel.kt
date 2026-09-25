package com.proyectotitulo.appcomerciojcc.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.ProfileRepository
import com.proyectotitulo.appcomerciojcc.domain.models.GetCommuneResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetPrivateProfileResponse
import com.proyectotitulo.appcomerciojcc.domain.models.ProfileUiState
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _uiState = MutableLiveData<ProfileUiState>(ProfileUiState.Loading)
    val uiState: LiveData<ProfileUiState> = _uiState

    private val _communesList = MutableLiveData<List<GetCommuneResponse.Commune>>(emptyList())
    val communesList: LiveData<List<GetCommuneResponse.Commune>> = _communesList

    init {
        loadProfile()
    }

    fun loadCommunes() {
        viewModelScope.launch {
            val result = repository.getCommunes()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _communesList.value = response.data ?: emptyList()
                } else {
                    _uiState.value = ProfileUiState.Error(
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure {error ->
                _uiState.value = ProfileUiState.Error(
                    errors = listOf(error.message ?: "Error de red inesperado")
                )
            }
        }
    }

    fun loadProfile() {
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

    fun updateDescription(newDescription: String, onResult: (Boolean, String?) -> Unit ) {
        viewModelScope.launch {
            val result = repository.updateProfileDescription(newDescription)
            result.onSuccess { response ->
                if (response.status == "success") {
                    val currentState = _uiState.value
                    if (currentState is ProfileUiState.Success) {
                        val updatedProfile = currentState.profileData?.copy(
                            profileDescription = response.data?.profileDescription ?: newDescription
                        )
                        _uiState.value = ProfileUiState.Success(updatedProfile)
                    } else {
                        loadProfile()
                    }
                    onResult(true, null)
                } else {
                    val errorMsg = response.errors?.firstOrNull() ?: response.message
                    onResult(false, errorMsg)
                }
            }.onFailure { error ->
                onResult(false, error.message ?: "Error al actualizar la descripción")
            }
        }
    }

    fun updateCommune(selectedCommune: GetCommuneResponse.Commune, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateCommune(selectedCommune.communeId)
            result.onSuccess { response ->
                if (response.status == "success") {
                    val currentState = _uiState.value
                    if (currentState is ProfileUiState.Success) {
                        val newCommune = GetPrivateProfileResponse.Profile.Commune(
                            communeId = response.data?.communeId ?: selectedCommune.communeId,
                            communeName = response.data?.communeName ?: selectedCommune.communeName,
                            region = response.data?.region ?: selectedCommune.region
                        )
                        _uiState.value = ProfileUiState.Success(
                            currentState.profileData?.copy(commune = newCommune)
                        )
                    }
                    onResult(true, null)
                } else {
                    val errorMsg = response.errors?.firstOrNull() ?: response.message
                    onResult(false, errorMsg)
                }
            }.onFailure { error ->
                onResult(false, error.message ?: "Error al actualizar la comuna")
            }
        }
    }

    fun updateGeoRadius(newRadius: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateGeoRadius(newRadius)
            result.onSuccess { response ->
                if (response.status == "success") {
                    val currentState = _uiState.value
                    if (currentState is ProfileUiState.Success) {
                        val updatedProfile = currentState.profileData?.copy(
                            geoRadius = response.data?.geoRadius ?: newRadius
                        )
                        _uiState.value = ProfileUiState.Success(updatedProfile)
                    } else {
                        loadProfile()
                    }
                    onResult(true, null)
                } else {
                    val errorMsg = response.errors?.firstOrNull() ?: response.message
                    onResult(false, errorMsg)
                }
            }.onFailure { error ->
                onResult(false, error.message ?: "Error al actualizar el radio de geolocalización")
            }
        }
    }
}