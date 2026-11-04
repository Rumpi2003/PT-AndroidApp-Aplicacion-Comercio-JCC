package com.proyectotitulo.appcomerciojcc.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.MatchmakingRepository
import com.proyectotitulo.appcomerciojcc.domain.models.GetMatchmakingResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTradeInventoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.HomeUiState
import com.proyectotitulo.appcomerciojcc.domain.models.PostTransactionRequest
import com.proyectotitulo.appcomerciojcc.domain.models.SelectedOfferedCard
import com.proyectotitulo.appcomerciojcc.domain.models.SelectedSolicitedCard
import com.proyectotitulo.appcomerciojcc.domain.models.TransactionState
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
        val activeUserCommune = if (activeUseGeo) false else currentState.useUserCommune

        if (!forceRefresh && currentState.recommendations.isNotEmpty()) {
            return
        }

        val limit = if (currentState.isAllOffersMode) 10 else 5

        viewModelScope.launch {
            if (forceRefresh && !currentState.isLoading) {
                _uiState.value = currentState.copy(
                    isRefreshing = true,
                    useGeolocation = activeUseGeo,
                    useUserCommune = activeUserCommune,
                    errors = null
                )
            } else {
                _uiState.value = currentState.copy(
                    isLoading = true,
                    useGeolocation = activeUseGeo,
                    useUserCommune = activeUserCommune,
                    errors = null
                )
            }

            val result = repository.getMatchmaking(
                limit = limit,
                offset = 0,
                useGeolocation = activeUseGeo,
                userCommune = activeUserCommune
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
                        useUserCommune = activeUserCommune,
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
                        useUserCommune = if (enabled) false else currentState.useUserCommune,
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

    fun toggleUserCommune(enabled: Boolean) {
        val currentState = _uiState.value ?: HomeUiState()
        if (currentState.useGeolocation) return

        _uiState.value = currentState.copy(useUserCommune = enabled)
        loadRecommendations(forceRefresh = true)
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

    fun openTransactionDialog(user: GetMatchmakingResponse.Data.Result.User) {
        val userId = user.userId ?: return
        _uiState.value = _uiState.value?.copy(
            transactionState = TransactionState(
                isOpen = true,
                step = 1,
                targetUser = user,
                isLoadingInventories = true
            )
        )
        fetchTransactionInventories(userId)
    }

    fun fetchTransactionInventories(otherUserId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value ?: HomeUiState()
            _uiState.value = currentState.copy(
                transactionState = currentState.transactionState.copy(
                    isLoadingInventories = true,
                    transactionError = null
                )
            )

            val otherInvResult = repository.getOtherInventory(otherUserId)
            val myInvResult = repository.getMyInventory(otherUserId)

            var otherInv: List<GetTradeInventoryResponse.Card> = emptyList()
            var myInv: List<GetTradeInventoryResponse.Card> = emptyList()
            var errorMessage: String? = null

            otherInvResult.onSuccess { response ->
                if (response.status == "success") {
                    otherInv = response.data ?: emptyList()
                } else {
                    errorMessage = response.errors?.firstOrNull() ?: response.message
                }
            }.onFailure { error ->
                errorMessage = error.message ?: "Error al cargar inventario del ofertador"
            }

            myInvResult.onSuccess { response ->
                if (response.status == "success") {
                    myInv = response.data ?: emptyList()
                } else {
                    if (errorMessage == null) {
                        errorMessage = response.errors?.firstOrNull() ?: response.message
                    }
                }
            }.onFailure { error ->
                if (errorMessage == null) {
                    errorMessage = error.message ?: "Error al cargar tu inventario"
                }
            }

            val latest = _uiState.value ?: HomeUiState()
            _uiState.value = latest.copy(
                transactionState = latest.transactionState.copy(
                    otherUserInventory = otherInv,
                    myInventory = myInv,
                    isLoadingInventories = false,
                    transactionError = errorMessage
                )
            )
        }
    }

    fun toggleSolicitedCardSelection(card: GetTradeInventoryResponse.Card, isSelected: Boolean) {
        val id = card.inventoryCardId ?: return
        val currentState = _uiState.value ?: return
        val txState = currentState.transactionState
        val currentMap = txState.selectedSolicitedCards.toMutableMap()

        if (isSelected) {
            currentMap[id] = SelectedSolicitedCard(card = card, quantity = 1, offeredPrice = card.price ?: 0)
        } else {
            currentMap.remove(id)
        }

        _uiState.value = currentState.copy(
            transactionState = txState.copy(selectedSolicitedCards = currentMap)
        )
    }

    fun updateSolicitedCardQuantity(inventoryCardId: Int, quantity: Int) {
        val currentState = _uiState.value ?: return
        val txState = currentState.transactionState
        val item = txState.selectedSolicitedCards[inventoryCardId] ?: return
        val maxStock = item.card.availableStock ?: 1
        val validQty = quantity.coerceIn(1, maxStock)

        val currentMap = txState.selectedSolicitedCards.toMutableMap()
        currentMap[inventoryCardId] = item.copy(quantity = validQty)

        _uiState.value = currentState.copy(
            transactionState = txState.copy(selectedSolicitedCards = currentMap)
        )
    }

    fun updateSolicitedCardOfferedPrice(inventoryCardId: Int, price: Int) {
        val currentState = _uiState.value ?: return
        val txState = currentState.transactionState
        val item = txState.selectedSolicitedCards[inventoryCardId] ?: return
        val validPrice = price.coerceAtLeast(0)

        val currentMap = txState.selectedSolicitedCards.toMutableMap()
        currentMap[inventoryCardId] = item.copy(offeredPrice = validPrice)

        _uiState.value = currentState.copy(
            transactionState = txState.copy(selectedSolicitedCards = currentMap)
        )
    }

    fun toggleOfferedCardSelection(card: GetTradeInventoryResponse.Card, isSelected: Boolean) {
        val id = card.inventoryCardId ?: return
        val currentState = _uiState.value ?: return
        val txState = currentState.transactionState
        val currentMap = txState.selectedOfferedCards.toMutableMap()

        if (isSelected) {
            currentMap[id] = SelectedOfferedCard(card = card, quantity = 1)
        } else {
            currentMap.remove(id)
        }

        _uiState.value = currentState.copy(
            transactionState = txState.copy(selectedOfferedCards = currentMap)
        )
    }

    fun updateOfferedCardQuantity(inventoryCardId: Int, quantity: Int) {
        val currentState = _uiState.value ?: return
        val txState = currentState.transactionState
        val item = txState.selectedOfferedCards[inventoryCardId] ?: return
        val maxStock = item.card.availableStock ?: 1
        val validQty = quantity.coerceIn(1, maxStock)

        val currentMap = txState.selectedOfferedCards.toMutableMap()
        currentMap[inventoryCardId] = item.copy(quantity = validQty)

        _uiState.value = currentState.copy(
            transactionState = txState.copy(selectedOfferedCards = currentMap)
        )
    }

    fun goToTransactionStep(step: Int) {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(
            transactionState = currentState.transactionState.copy(step = step)
        )
    }

    fun submitTransaction() {
        val currentState = _uiState.value ?: return
        val txState = currentState.transactionState
        val otherUserId = txState.targetUser?.userId ?: return

        val solicitedList = txState.selectedSolicitedCards.values.map {
            PostTransactionRequest.SolicitedCard(
                inventoryCardId = it.card.inventoryCardId!!,
                quantity = it.quantity,
                offeredPrice = it.offeredPrice
            )
        }

        val offeredList = txState.selectedOfferedCards.values.map {
            PostTransactionRequest.OfferedCard(
                inventoryCardId = it.card.inventoryCardId!!,
                quantity = it.quantity
            )
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                transactionState = txState.copy(
                    isSubmittingTransaction = true,
                    transactionError = null
                )
            )

            val result = repository.postTransaction(
                otherUserId = otherUserId,
                solicitedCards = solicitedList,
                offeredCards = offeredList
            )

            val latest = _uiState.value ?: HomeUiState()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latest.copy(
                        transactionState = latest.transactionState.copy(
                            isSubmittingTransaction = false,
                            transactionSuccessMessage = response.message ?: "¡Transacción creada exitosamente!",
                            step = 4
                        )
                    )
                    // Recargar emparejamiento inmediatamente tras éxito de transacción
                    loadRecommendations(forceRefresh = true)
                } else {
                    _uiState.value = latest.copy(
                        transactionState = latest.transactionState.copy(
                            isSubmittingTransaction = false,
                            transactionError = response.errors?.firstOrNull() ?: response.message
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.value = latest.copy(
                    transactionState = latest.transactionState.copy(
                        isSubmittingTransaction = false,
                        transactionError = error.message ?: "Error al crear la transacción"
                    )
                )
            }
        }
    }

    fun retryTransaction() {
        val currentState = _uiState.value ?: return
        val targetUser = currentState.transactionState.targetUser ?: return
        openTransactionDialog(targetUser)
    }

    fun closeTransactionDialog() {
        val currentState = _uiState.value ?: return
        val wasSuccess = currentState.transactionState.step == 4 && currentState.transactionState.transactionSuccessMessage != null

        _uiState.value = currentState.copy(
            transactionState = TransactionState(isOpen = false)
        )

        if (wasSuccess) {
            loadRecommendations(forceRefresh = true)
        }
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
                useGeolocation = currentState.useGeolocation,
                userCommune = if (currentState.useGeolocation) false else currentState.useUserCommune
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
