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
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = MatchmakingRepository()

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        loadRecommendations()
        loadPopularCards()
        loadInitialGeoMode()
    }

    fun loadPopularCards() {
        viewModelScope.launch {
            val currentState = _uiState.value ?: HomeUiState()
            _uiState.value = currentState.copy(isLoadingPopularCards = true)

            val result = repository.getMostPopularCards()
            val latest = _uiState.value ?: HomeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latest.copy(
                        popularCards = response.data?.take(10) ?: emptyList(),
                        isLoadingPopularCards = false
                    )
                } else {
                    _uiState.value = latest.copy(isLoadingPopularCards = false)
                }
            }.onFailure {
                _uiState.value = latest.copy(isLoadingPopularCards = false)
            }
        }
    }

    fun loadInitialGeoMode() {
        viewModelScope.launch {
            val geoResult = repository.getGeoMode()
            val currentState = _uiState.value ?: HomeUiState()

            geoResult.onSuccess { response ->
                if (response.status == "success") {
                    val geoModeActive = response.data?.geoMode == true
                    _uiState.value = currentState.copy(useGeolocation = geoModeActive)
                    if (geoModeActive) {
                        loadRecommendations(forceRefresh = true)
                    }
                }
            }
        }
    }

    fun loadRecommendations(forceRefresh: Boolean = false) {
        val currentState = _uiState.value ?: HomeUiState()
        if (!forceRefresh && currentState.recommendations.isNotEmpty()) {
            return
        }

        if (forceRefresh) {
            loadPopularCards()
        }

        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.value = currentState.copy(isRefreshing = true, errors = null)
            } else {
                _uiState.value = currentState.copy(isLoading = true, errors = null)
            }

            val result = repository.getMatchmaking(
                limit = 5,
                offset = 0,
                useGeolocation = currentState.useGeolocation,
                userCommune = currentState.useUserCommune
            )

            val latestState = _uiState.value ?: HomeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latestState.copy(
                        recommendations = response.data?.results ?: emptyList(),
                        totalRecommendations = response.data?.total ?: 0,
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
                    errors = listOf(error.message ?: "Error de red al cargar recomendaciones")
                )
            }
        }
    }

    fun loadAllOffers(forceRefresh: Boolean = false) {
        val currentState = _uiState.value ?: HomeUiState()

        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.value = currentState.copy(
                    isAllOffersMode = true,
                    isLoading = true,
                    errors = null
                )
            } else {
                _uiState.value = currentState.copy(
                    isLoading = true,
                    errors = null
                )
            }

            val result = repository.getMatchmaking(
                limit = 50,
                offset = 0,
                useGeolocation = currentState.useGeolocation,
                userCommune = currentState.useUserCommune
            )

            val latestState = _uiState.value ?: HomeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latestState.copy(
                        recommendations = response.data?.results ?: emptyList(),
                        totalRecommendations = response.data?.total ?: 0,
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
                    errors = listOf(error.message ?: "Error de red al cargar todas las ofertas")
                )
            }
        }
    }

    fun exitAllOffersMode() {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(
            isAllOffersMode = false
        )
        loadRecommendations(forceRefresh = true)
    }

    fun toggleUserCommuneFilter(enabled: Boolean) {
        val currentState = _uiState.value ?: return
        if (currentState.useGeolocation) return

        _uiState.value = currentState.copy(useUserCommune = enabled)

        if (currentState.isAllOffersMode) {
            loadAllOffers(forceRefresh = true)
        } else {
            loadRecommendations(forceRefresh = true)
        }
    }

    fun toggleGeolocationFilter(enabled: Boolean, onNeedsPermission: () -> Unit) {
        val currentState = _uiState.value ?: return

        if (enabled) {
            onNeedsPermission()
        } else {
            viewModelScope.launch {
                repository.updateGeoMode(false)
                val latest = _uiState.value ?: HomeUiState()
                _uiState.value = latest.copy(useGeolocation = false)

                if (latest.isAllOffersMode) {
                    loadAllOffers(forceRefresh = true)
                } else {
                    loadRecommendations(forceRefresh = true)
                }
            }
        }
    }

    fun sendLocationAndUpdateMatchmaking(latitude: Float, longitude: Float) {
        viewModelScope.launch {
            val currentState = _uiState.value ?: HomeUiState()
            _uiState.value = currentState.copy(isLoading = true, errors = null)

            // 1. Primero activar el modo geolocalización en el servidor
            val geoModeResult = repository.updateGeoMode(true)

            geoModeResult.onSuccess { geoResponse ->
                if (geoResponse.status == "success") {
                    // 2. Una vez activo el modo geo, enviar las coordenadas GPS
                    val locationResult = repository.updateLocation(latitude, longitude)

                    locationResult.onSuccess { locResponse ->
                        if (locResponse.status == "success") {
                            val latest = _uiState.value ?: HomeUiState()
                            _uiState.value = latest.copy(
                                useGeolocation = true,
                                useUserCommune = false,
                                isLoading = false
                            )

                            if (latest.isAllOffersMode) {
                                loadAllOffers(forceRefresh = true)
                            } else {
                                loadRecommendations(forceRefresh = true)
                            }
                        } else {
                            repository.updateGeoMode(false)
                            val latest = _uiState.value ?: HomeUiState()
                            _uiState.value = latest.copy(
                                useGeolocation = false,
                                isLoading = false,
                                errors = locResponse.errors ?: listOf(locResponse.message)
                            )
                        }
                    }.onFailure { error ->
                        repository.updateGeoMode(false)
                        val latest = _uiState.value ?: HomeUiState()
                        _uiState.value = latest.copy(
                            useGeolocation = false,
                            isLoading = false,
                            errors = listOf(error.message ?: "Error al enviar ubicación GPS")
                        )
                    }
                } else {
                    repository.updateGeoMode(false)
                    val latest = _uiState.value ?: HomeUiState()
                    _uiState.value = latest.copy(
                        useGeolocation = false,
                        isLoading = false,
                        errors = geoResponse.errors ?: listOf(geoResponse.message)
                    )
                }
            }.onFailure { error ->
                repository.updateGeoMode(false)
                val latest = _uiState.value ?: HomeUiState()
                _uiState.value = latest.copy(
                    useGeolocation = false,
                    isLoading = false,
                    errors = listOf(error.message ?: "Error al activar modo geolocalización")
                )
            }
        }
    }

    fun updatePeriodicLocation(latitude: Float, longitude: Float) {
        viewModelScope.launch {
            val locationResult = repository.updateLocation(latitude, longitude)

            locationResult.onSuccess { locResponse ->
                if (locResponse.status == "success") {
                    val currentState = _uiState.value ?: HomeUiState()
                    val result = repository.getMatchmaking(
                        limit = if (currentState.isAllOffersMode) 50 else 5,
                        offset = 0,
                        useGeolocation = true,
                        userCommune = false
                    )

                    result.onSuccess { response ->
                        if (response.status == "success") {
                            val latest = _uiState.value ?: HomeUiState()
                            _uiState.value = latest.copy(
                                recommendations = response.data?.results ?: emptyList(),
                                totalRecommendations = response.data?.total ?: 0
                            )
                        }
                    }
                }
            }
        }
    }

    fun disableGeoModeOnLogout(onComplete: () -> Unit) {
        _uiState.value = (_uiState.value ?: HomeUiState()).copy(useGeolocation = false)
        onComplete()
    }

    fun openTransactionDialog(targetUser: GetMatchmakingResponse.Data.Result.User) {
        val otherUserId = targetUser.userId ?: return
        val currentState = _uiState.value ?: HomeUiState()

        _uiState.value = currentState.copy(
            transactionState = TransactionState(
                isOpen = true,
                step = 1,
                targetUser = targetUser,
                isLoadingInventories = true
            )
        )

        fetchTransactionInventories(otherUserId)
    }

    fun fetchTransactionInventories(otherUserId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value ?: HomeUiState()

            val otherInvResult = repository.getOtherInventory(otherUserId)
            val myInvResult = repository.getMyInventory(otherUserId)

            var errorMessage: String? = null
            var otherInv: List<GetTradeInventoryResponse.Card> = emptyList()
            var myInv: List<GetTradeInventoryResponse.Card> = emptyList()

            otherInvResult.onSuccess { response ->
                if (response.status == "success") {
                    otherInv = response.data ?: emptyList()
                } else {
                    errorMessage = response.errors?.firstOrNull() ?: response.message
                }
            }.onFailure { error ->
                errorMessage = error.message ?: "Error al cargar inventario del usuario"
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

    fun updateSolicitedCardPrice(inventoryCardId: Int, price: Int) {
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

    fun submitTransaction(onSuccess: () -> Unit = {}) {
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
                    loadRecommendations(forceRefresh = true)
                    onSuccess()
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

    fun closeTransactionDialog() {
        val currentState = _uiState.value ?: return
        val wasSuccess = currentState.transactionState.step == 4 && currentState.transactionState.transactionSuccessMessage != null

        _uiState.value = currentState.copy(
            transactionState = TransactionState(isOpen = false)
        )

        if (wasSuccess) {
            if (currentState.isAllOffersMode) {
                loadAllOffers(forceRefresh = true)
            } else {
                loadRecommendations(forceRefresh = true)
            }
        }
    }

    fun clearData() {
        _uiState.value = HomeUiState()
    }
}
