package com.proyectotitulo.appcomerciojcc.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.MatchmakingRepository
import com.proyectotitulo.appcomerciojcc.domain.models.HomeUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = MatchmakingRepository()

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    private var locationTimerJob: Job? = null

    init {
        loadInitialGeoMode()
    }

    fun loadInitialGeoMode() {
        viewModelScope.launch {
            _uiState.value = (_uiState.value ?: HomeUiState()).copy(isLoading = true, errors = null)

            val geoResult = repository.getGeoMode()
            var isGeoActive = false

            geoResult.onSuccess { response ->
                if (response.status == "success") {
                    isGeoActive = response.data?.geoMode ?: false
                }
            }

            _uiState.value = (_uiState.value ?: HomeUiState()).copy(useGeolocation = isGeoActive)
            loadRecommendations(forceRefresh = true, overrideUseGeo = isGeoActive)
        }
    }

    fun loadRecommendations(forceRefresh: Boolean = false, overrideUseGeo: Boolean? = null) {
        val currentState = _uiState.value ?: HomeUiState()
        val activeUseGeo = overrideUseGeo ?: currentState.useGeolocation

        if (!forceRefresh && currentState.recommendations.isNotEmpty()) {
            return
        }

        val limit = if (currentState.isAllOffersMode) 10 else 5

        viewModelScope.launch {
            if (forceRefresh && !currentState.isLoading) {
                _uiState.value = currentState.copy(isRefreshing = true, useGeolocation = activeUseGeo, errors = null)
            } else {
                _uiState.value = currentState.copy(isLoading = true, useGeolocation = activeUseGeo, errors = null)
            }

            val result = repository.getMatchmaking(
                limit = limit,
                offset = 0,
                useGeolocation = activeUseGeo
            )
            val latestState = _uiState.value ?: HomeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latestState.copy(
                        recommendations = response.data?.results ?: emptyList(),
                        totalRecommendations = response.data?.total ?: 0,
                        currentPage = 1,
                        limitPerPage = limit,
                        useGeolocation = activeUseGeo,
                        isLoading = false,
                        isRefreshing = false
                    )
                } else {
                    _uiState.value = latestState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = latestState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errors = listOf(error.message ?: "Error al cargar las recomendaciones")
                )
            }
        }
    }

    fun toggleGeoMode(
        newGeoMode: Boolean,
        onRequestLocation: (((Float, Float) -> Unit) -> Unit)
    ) {
        val currentState = _uiState.value ?: HomeUiState()

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoadingGeoMode = true, errors = null)

            val result = repository.updateGeoMode(newGeoMode)

            result.onSuccess { response ->
                if (response.status == "success") {
                    val enabled = response.data?.geoMode ?: newGeoMode
                    _uiState.value = (_uiState.value ?: HomeUiState()).copy(
                        useGeolocation = enabled,
                        isLoadingGeoMode = false
                    )

                    if (enabled) {
                        onRequestLocation { lat, lng ->
                            sendLocationAndUpdateMatchmaking(lat, lng)
                        }
                        startPeriodicLocationTimer(onRequestLocation)
                    } else {
                        stopPeriodicLocationTimer()
                        loadRecommendations(forceRefresh = true, overrideUseGeo = false)
                    }
                } else {
                    _uiState.value = (_uiState.value ?: HomeUiState()).copy(
                        isLoadingGeoMode = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = (_uiState.value ?: HomeUiState()).copy(
                    isLoadingGeoMode = false,
                    errors = listOf(error.message ?: "Error al actualizar el modo de geolocalización")
                )
            }
        }
    }

    fun sendLocationAndUpdateMatchmaking(latitude: Float, longitude: Float) {
        viewModelScope.launch {
            repository.updateLocation(latitude, longitude)
            loadRecommendations(forceRefresh = true, overrideUseGeo = true)
        }
    }

    private fun startPeriodicLocationTimer(onRequestLocation: (((Float, Float) -> Unit) -> Unit)) {
        stopPeriodicLocationTimer()
        locationTimerJob = viewModelScope.launch {
            while (isActive && _uiState.value?.useGeolocation == true) {
                delay(60_000) // 1 minuto
                if (_uiState.value?.useGeolocation == true) {
                    onRequestLocation { lat, lng ->
                        viewModelScope.launch {
                            repository.updateLocation(lat, lng)
                        }
                    }
                }
            }
        }
    }

    private fun stopPeriodicLocationTimer() {
        locationTimerJob?.cancel()
        locationTimerJob = null
    }

    fun disableGeoModeOnLogout(onComplete: () -> Unit) {
        stopPeriodicLocationTimer()
        val isGeoActive = _uiState.value?.useGeolocation == true
        if (isGeoActive) {
            viewModelScope.launch {
                try {
                    repository.updateGeoMode(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    clearData()
                    onComplete()
                }
            }
        } else {
            clearData()
            onComplete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPeriodicLocationTimer()
        if (_uiState.value?.useGeolocation == true) {
            viewModelScope.launch {
                try {
                    repository.updateGeoMode(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun switchToAllOffersMode() {
        val currentState = _uiState.value ?: HomeUiState()
        _uiState.value = currentState.copy(
            isAllOffersMode = true,
            limitPerPage = 10,
            currentPage = 1,
            isLoading = true
        )
        loadRecommendations(forceRefresh = true)
    }

    fun exitAllOffersMode() {
        val currentState = _uiState.value ?: HomeUiState()
        _uiState.value = currentState.copy(
            isAllOffersMode = false,
            limitPerPage = 5,
            currentPage = 1,
            isLoading = true
        )
        loadRecommendations(forceRefresh = true)
    }

    fun loadNextPage() {
        val currentState = _uiState.value ?: return
        if (currentState.isLoadingNextPage || currentState.recommendations.size >= currentState.totalRecommendations) {
            return
        }

        val nextOffset = currentState.recommendations.size

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoadingNextPage = true, errors = null)

            val result = repository.getMatchmaking(
                limit = currentState.limitPerPage,
                offset = nextOffset,
                useGeolocation = currentState.useGeolocation
            )

            result.onSuccess { response ->
                val latest = _uiState.value ?: HomeUiState()
                if (response.status == "success") {
                    val newItems = response.data?.results ?: emptyList()
                    _uiState.value = latest.copy(
                        recommendations = latest.recommendations + newItems,
                        totalRecommendations = response.data?.total ?: latest.totalRecommendations,
                        isLoadingNextPage = false
                    )
                } else {
                    _uiState.value = latest.copy(
                        isLoadingNextPage = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                val latest = _uiState.value ?: HomeUiState()
                _uiState.value = latest.copy(
                    isLoadingNextPage = false,
                    errors = listOf(error.message ?: "Error al cargar más ofertas")
                )
            }
        }
    }

    fun clearData() {
        stopPeriodicLocationTimer()
        _uiState.value = HomeUiState()
    }
}
