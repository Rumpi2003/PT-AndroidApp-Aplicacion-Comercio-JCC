package com.proyectotitulo.appcomerciojcc.ui.trade

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.MatchmakingRepository
import com.proyectotitulo.appcomerciojcc.data.repository.TradeRepository
import com.proyectotitulo.appcomerciojcc.domain.models.GetTradeInventoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTransactionsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.NegotiationState
import com.proyectotitulo.appcomerciojcc.domain.models.PutCounterOfferRequest
import com.proyectotitulo.appcomerciojcc.domain.models.SelectedNegotiationOfferedCard
import com.proyectotitulo.appcomerciojcc.domain.models.SelectedNegotiationSolicitedCard
import com.proyectotitulo.appcomerciojcc.domain.models.TradeUiState
import kotlinx.coroutines.launch

class TradeViewModel : ViewModel() {

    private val repository = TradeRepository()
    private val matchmakingRepository = MatchmakingRepository()

    private val _uiState = MutableLiveData<TradeUiState>(TradeUiState())
    val uiState: LiveData<TradeUiState> = _uiState

    init {
        loadActiveTransactions()
    }

    fun loadActiveTransactions(forceRefresh: Boolean = false) {
        val currentState = _uiState.value ?: TradeUiState()
        if (!forceRefresh && (currentState.pendingTransactions.isNotEmpty() || currentState.myNegotiatingTransactions.isNotEmpty() || currentState.receivedNegotiatingTransactions.isNotEmpty())) {
            return
        }

        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.value = currentState.copy(isRefreshing = true, errors = null)
            } else {
                _uiState.value = currentState.copy(isLoading = true, errors = null)
            }

            val result = repository.getTransactions(view = "activas", limit = 50, offset = 0)
            val latestState = _uiState.value ?: TradeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    val allActive = response.data?.results ?: emptyList()

                    val pending = allActive.filter { it.transactionState == "PENDIENTE" }.take(5)
                    val myNegotiating = allActive.filter { it.transactionState == "EN_NEGOCIACION" && it.role == "PROPONENTE" }.take(10)
                    val receivedNegotiating = allActive.filter { it.transactionState == "EN_NEGOCIACION" && it.role == "DESTINATARIO" }

                    _uiState.value = latestState.copy(
                        pendingTransactions = pending,
                        myNegotiatingTransactions = myNegotiating,
                        receivedNegotiatingTransactions = receivedNegotiating,
                        receivedTotal = response.data?.total ?: receivedNegotiating.size,
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
                    errors = listOf(error.message ?: "Error al cargar transacciones activas")
                )
            }
        }
    }

    fun loadMoreReceivedNegotiating() {
        val currentState = _uiState.value ?: return
        if (currentState.isLoadingMoreReceived || currentState.receivedNegotiatingTransactions.size >= currentState.receivedTotal) {
            return
        }

        val nextOffset = currentState.receivedNegotiatingTransactions.size

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoadingMoreReceived = true, errors = null)

            val result = repository.getTransactions(
                view = "activas",
                state = "EN_NEGOCIACION",
                limit = 10,
                offset = nextOffset
            )

            result.onSuccess { response ->
                val latest = _uiState.value ?: TradeUiState()
                if (response.status == "success") {
                    val newReceived = (response.data?.results ?: emptyList()).filter { it.role == "DESTINATARIO" }
                    _uiState.value = latest.copy(
                        receivedNegotiatingTransactions = latest.receivedNegotiatingTransactions + newReceived,
                        receivedTotal = response.data?.total ?: latest.receivedTotal,
                        isLoadingMoreReceived = false
                    )
                } else {
                    _uiState.value = latest.copy(
                        isLoadingMoreReceived = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                val latest = _uiState.value ?: TradeUiState()
                _uiState.value = latest.copy(
                    isLoadingMoreReceived = false,
                    errors = listOf(error.message ?: "Error al cargar más transacciones recibidas")
                )
            }
        }
    }

    fun openNegotiationDialog(transactionId: Int, counterpart: GetTransactionsResponse.Data.Result.Counterpart) {
        val otherUserId = counterpart.userId ?: return
        val currentState = _uiState.value ?: TradeUiState()

        _uiState.value = currentState.copy(
            negotiationState = NegotiationState(
                isOpen = true,
                step = 3,
                transactionId = transactionId,
                targetUser = counterpart,
                isLoadingDetails = true
            )
        )

        fetchNegotiationDetailsAndInventories(transactionId, otherUserId)
    }

    fun fetchNegotiationDetailsAndInventories(transactionId: Int, otherUserId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value ?: TradeUiState()

            val txDetailResult = repository.getTransactions(transactionId)
            val otherInvResult = matchmakingRepository.getOtherInventory(otherUserId)
            val myInvResult = matchmakingRepository.getMyInventory(otherUserId)

            var errorMessage: String? = null
            var txDetails: com.proyectotitulo.appcomerciojcc.domain.models.GetTransactionResponse.Data? = null
            var otherInv: List<GetTradeInventoryResponse.Card> = emptyList()
            var myInv: List<GetTradeInventoryResponse.Card> = emptyList()

            txDetailResult.onSuccess { response ->
                if (response.status == "success") {
                    txDetails = response.data
                } else {
                    errorMessage = response.errors?.firstOrNull() ?: response.message
                }
            }.onFailure { error ->
                errorMessage = error.message ?: "Error al obtener detalle de transacción"
            }

            otherInvResult.onSuccess { response ->
                if (response.status == "success") {
                    otherInv = response.data ?: emptyList()
                }
            }

            myInvResult.onSuccess { response ->
                if (response.status == "success") {
                    myInv = response.data ?: emptyList()
                }
            }

            // Mapear cartas actualmente involucradas en la transacción
            val solicitedMap = mutableMapOf<Int, SelectedNegotiationSolicitedCard>()
            val offeredMap = mutableMapOf<Int, SelectedNegotiationOfferedCard>()

            txDetails?.cards?.forEach { card ->
                val invId = card.inventoryCardId ?: return@forEach
                val qty = card.quantity ?: 1
                val price = card.agreedUponPrice ?: 0

                if (card.direction == "RECIBO") {
                    val matchingInventoryCard = otherInv.find { it.inventoryCardId == invId } ?: GetTradeInventoryResponse.Card(
                        inventoryCardId = invId,
                        cardId = card.cardId,
                        cardName = card.cardName,
                        set = card.set,
                        imageUrl = card.imageUrl,
                        miniatureUrl = card.miniatureUrl,
                        condition = card.condition,
                        language = card.language,
                        price = price,
                        availableStock = qty
                    )
                    solicitedMap[invId] = SelectedNegotiationSolicitedCard(
                        card = matchingInventoryCard,
                        quantity = qty,
                        offeredPrice = price
                    )
                } else if (card.direction == "ENTREGO") {
                    val matchingInventoryCard = myInv.find { it.inventoryCardId == invId } ?: GetTradeInventoryResponse.Card(
                        inventoryCardId = invId,
                        cardId = card.cardId,
                        cardName = card.cardName,
                        set = card.set,
                        imageUrl = card.imageUrl,
                        miniatureUrl = card.miniatureUrl,
                        condition = card.condition,
                        language = card.language,
                        price = price,
                        availableStock = qty
                    )
                    offeredMap[invId] = SelectedNegotiationOfferedCard(
                        card = matchingInventoryCard,
                        quantity = qty,
                        offeredPrice = price
                    )
                }
            }

            val latest = _uiState.value ?: TradeUiState()
            _uiState.value = latest.copy(
                negotiationState = latest.negotiationState.copy(
                    initialTransactionDetails = txDetails,
                    otherUserInventory = otherInv,
                    myInventory = myInv,
                    selectedSolicitedCards = solicitedMap,
                    selectedOfferedCards = offeredMap,
                    hasModifications = false,
                    isLoadingDetails = false,
                    negotiationError = errorMessage
                )
            )
        }
    }

    fun toggleNegotiationSolicitedCardSelection(card: GetTradeInventoryResponse.Card, isSelected: Boolean) {
        val id = card.inventoryCardId ?: return
        val currentState = _uiState.value ?: return
        val negState = currentState.negotiationState
        val currentMap = negState.selectedSolicitedCards.toMutableMap()

        if (isSelected) {
            currentMap[id] = SelectedNegotiationSolicitedCard(card = card, quantity = 1, offeredPrice = card.price ?: 0)
        } else {
            currentMap.remove(id)
        }

        _uiState.value = currentState.copy(
            negotiationState = negState.copy(
                selectedSolicitedCards = currentMap,
                hasModifications = true
            )
        )
    }

    fun updateNegotiationSolicitedCardQuantity(inventoryCardId: Int, quantity: Int) {
        val currentState = _uiState.value ?: return
        val negState = currentState.negotiationState
        val item = negState.selectedSolicitedCards[inventoryCardId] ?: return
        val maxStock = item.card.availableStock ?: 1
        val validQty = quantity.coerceIn(1, maxStock)

        val currentMap = negState.selectedSolicitedCards.toMutableMap()
        currentMap[inventoryCardId] = item.copy(quantity = validQty)

        _uiState.value = currentState.copy(
            negotiationState = negState.copy(
                selectedSolicitedCards = currentMap,
                hasModifications = true
            )
        )
    }

    fun updateNegotiationSolicitedCardPrice(inventoryCardId: Int, price: Int) {
        val currentState = _uiState.value ?: return
        val negState = currentState.negotiationState
        val item = negState.selectedSolicitedCards[inventoryCardId] ?: return
        val validPrice = price.coerceAtLeast(0)

        val currentMap = negState.selectedSolicitedCards.toMutableMap()
        currentMap[inventoryCardId] = item.copy(offeredPrice = validPrice)

        _uiState.value = currentState.copy(
            negotiationState = negState.copy(
                selectedSolicitedCards = currentMap,
                hasModifications = true
            )
        )
    }

    fun toggleNegotiationOfferedCardSelection(card: GetTradeInventoryResponse.Card, isSelected: Boolean) {
        val id = card.inventoryCardId ?: return
        val currentState = _uiState.value ?: return
        val negState = currentState.negotiationState
        val currentMap = negState.selectedOfferedCards.toMutableMap()

        if (isSelected) {
            currentMap[id] = SelectedNegotiationOfferedCard(card = card, quantity = 1, offeredPrice = card.price ?: 0)
        } else {
            currentMap.remove(id)
        }

        _uiState.value = currentState.copy(
            negotiationState = negState.copy(
                selectedOfferedCards = currentMap,
                hasModifications = true
            )
        )
    }

    fun updateNegotiationOfferedCardQuantity(inventoryCardId: Int, quantity: Int) {
        val currentState = _uiState.value ?: return
        val negState = currentState.negotiationState
        val item = negState.selectedOfferedCards[inventoryCardId] ?: return
        val maxStock = item.card.availableStock ?: 1
        val validQty = quantity.coerceIn(1, maxStock)

        val currentMap = negState.selectedOfferedCards.toMutableMap()
        currentMap[inventoryCardId] = item.copy(quantity = validQty)

        _uiState.value = currentState.copy(
            negotiationState = negState.copy(
                selectedOfferedCards = currentMap,
                hasModifications = true
            )
        )
    }

    fun updateNegotiationOfferedCardPrice(inventoryCardId: Int, price: Int) {
        val currentState = _uiState.value ?: return
        val negState = currentState.negotiationState
        val item = negState.selectedOfferedCards[inventoryCardId] ?: return
        val validPrice = price.coerceAtLeast(0)

        val currentMap = negState.selectedOfferedCards.toMutableMap()
        currentMap[inventoryCardId] = item.copy(offeredPrice = validPrice)

        _uiState.value = currentState.copy(
            negotiationState = negState.copy(
                selectedOfferedCards = currentMap,
                hasModifications = true
            )
        )
    }

    fun goToNegotiationStep(step: Int) {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(
            negotiationState = currentState.negotiationState.copy(step = step)
        )
    }

    fun acceptNegotiation() {
        val currentState = _uiState.value ?: return
        val negState = currentState.negotiationState
        val txId = negState.transactionId

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                negotiationState = negState.copy(isSubmittingAction = true, negotiationError = null)
            )

            val result = repository.acceptTransaction(txId)
            val latest = _uiState.value ?: TradeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latest.copy(
                        negotiationState = latest.negotiationState.copy(
                            isSubmittingAction = false,
                            negotiationSuccessMessage = response.message ?: "¡Transacción aceptada exitosamente!",
                            step = 4
                        )
                    )
                    loadActiveTransactions(forceRefresh = true)
                } else {
                    _uiState.value = latest.copy(
                        negotiationState = latest.negotiationState.copy(
                            isSubmittingAction = false,
                            negotiationError = response.errors?.firstOrNull() ?: response.message
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.value = latest.copy(
                    negotiationState = latest.negotiationState.copy(
                        isSubmittingAction = false,
                        negotiationError = error.message ?: "Error al aceptar la transacción"
                    )
                )
            }
        }
    }

    fun declineNegotiation() {
        val currentState = _uiState.value ?: return
        val negState = currentState.negotiationState
        val txId = negState.transactionId

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                negotiationState = negState.copy(isSubmittingAction = true, negotiationError = null)
            )

            val result = repository.declineTransaction(txId)
            val latest = _uiState.value ?: TradeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latest.copy(
                        negotiationState = latest.negotiationState.copy(
                            isSubmittingAction = false,
                            negotiationSuccessMessage = response.message ?: "Transacción rechazada",
                            step = 4
                        )
                    )
                    loadActiveTransactions(forceRefresh = true)
                } else {
                    _uiState.value = latest.copy(
                        negotiationState = latest.negotiationState.copy(
                            isSubmittingAction = false,
                            negotiationError = response.errors?.firstOrNull() ?: response.message
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.value = latest.copy(
                    negotiationState = latest.negotiationState.copy(
                        isSubmittingAction = false,
                        negotiationError = error.message ?: "Error al rechazar la transacción"
                    )
                )
            }
        }
    }

    fun submitCounterOffer() {
        val currentState = _uiState.value ?: return
        val negState = currentState.negotiationState
        val txId = negState.transactionId

        val solicitedList = negState.selectedSolicitedCards.values.map {
            PutCounterOfferRequest.Card(
                cardId = it.card.inventoryCardId!!,
                quantity = it.quantity,
                offeredPrice = it.offeredPrice
            )
        }

        val offeredList = negState.selectedOfferedCards.values.map {
            PutCounterOfferRequest.Card(
                cardId = it.card.inventoryCardId!!,
                quantity = it.quantity,
                offeredPrice = it.offeredPrice
            )
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                negotiationState = negState.copy(isSubmittingAction = true, negotiationError = null)
            )

            val result = repository.putCounterOffer(
                transactionId = txId,
                solicitedCards = solicitedList,
                offeredCards = offeredList
            )

            val latest = _uiState.value ?: TradeUiState()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latest.copy(
                        negotiationState = latest.negotiationState.copy(
                            isSubmittingAction = false,
                            negotiationSuccessMessage = response.message ?: "¡Contraoferta enviada exitosamente!",
                            step = 4
                        )
                    )
                    loadActiveTransactions(forceRefresh = true)
                } else {
                    _uiState.value = latest.copy(
                        negotiationState = latest.negotiationState.copy(
                            isSubmittingAction = false,
                            negotiationError = response.errors?.firstOrNull() ?: response.message
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.value = latest.copy(
                    negotiationState = latest.negotiationState.copy(
                        isSubmittingAction = false,
                        negotiationError = error.message ?: "Error al enviar la contraoferta"
                    )
                )
            }
        }
    }

    fun closeNegotiationDialog() {
        val currentState = _uiState.value ?: return
        val wasSuccess = currentState.negotiationState.step == 4 && currentState.negotiationState.negotiationSuccessMessage != null

        _uiState.value = currentState.copy(
            negotiationState = NegotiationState(isOpen = false)
        )

        if (wasSuccess) {
            loadActiveTransactions(forceRefresh = true)
        }
    }

    fun switchToHistoryView() {
        _uiState.value = (_uiState.value ?: TradeUiState()).copy(
            isHistoryView = true,
            historyTransactions = emptyList(),
            historyTotal = 0,
            selectedHistoryFilter = null
        )
        loadHistoryTransactions(forceRefresh = true)
    }

    fun exitHistoryView() {
        _uiState.value = (_uiState.value ?: TradeUiState()).copy(
            isHistoryView = false
        )
        loadActiveTransactions(forceRefresh = true)
    }

    fun filterHistoryByState(stateFilter: String?) {
        _uiState.value = (_uiState.value ?: TradeUiState()).copy(
            selectedHistoryFilter = stateFilter,
            historyTransactions = emptyList(),
            historyTotal = 0
        )
        loadHistoryTransactions(forceRefresh = true)
    }

    fun loadHistoryTransactions(forceRefresh: Boolean = false) {
        val currentState = _uiState.value ?: TradeUiState()
        val filter = currentState.selectedHistoryFilter

        val currentOffset = if (forceRefresh) 0 else currentState.historyTransactions.size

        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.value = currentState.copy(isLoading = true, errors = null)
            } else {
                _uiState.value = currentState.copy(isLoadingMoreHistory = true, errors = null)
            }

            val result = repository.getTransactions(
                view = "historial",
                state = filter,
                limit = 10,
                offset = currentOffset
            )

            val latest = _uiState.value ?: TradeUiState()
            result.onSuccess { response ->
                if (response.status == "success") {
                    val newItems = response.data?.results ?: emptyList()
                    val updatedHistory = if (forceRefresh) newItems else (latest.historyTransactions + newItems)

                    _uiState.value = latest.copy(
                        historyTransactions = updatedHistory,
                        historyTotal = response.data?.total ?: updatedHistory.size,
                        isLoading = false,
                        isLoadingMoreHistory = false,
                        isRefreshing = false
                    )
                } else {
                    _uiState.value = latest.copy(
                        isLoading = false,
                        isLoadingMoreHistory = false,
                        isRefreshing = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = latest.copy(
                    isLoading = false,
                    isLoadingMoreHistory = false,
                    isRefreshing = false,
                    errors = listOf(error.message ?: "Error al cargar el historial")
                )
            }
        }
    }

    fun clearData() {
        _uiState.value = TradeUiState()
    }
}
