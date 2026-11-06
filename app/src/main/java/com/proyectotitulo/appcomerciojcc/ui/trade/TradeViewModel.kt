package com.proyectotitulo.appcomerciojcc.ui.trade

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.remote.SocketChatManager
import com.proyectotitulo.appcomerciojcc.data.repository.MatchmakingRepository
import com.proyectotitulo.appcomerciojcc.data.repository.ProfileRepository
import com.proyectotitulo.appcomerciojcc.data.repository.TradeRepository
import com.proyectotitulo.appcomerciojcc.domain.models.CancelConfirmationState
import com.proyectotitulo.appcomerciojcc.domain.models.ChatState
import com.proyectotitulo.appcomerciojcc.domain.models.GetMessageHistoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTradeInventoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTransactionsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.NegotiationState
import com.proyectotitulo.appcomerciojcc.domain.models.PendingSummaryState
import com.proyectotitulo.appcomerciojcc.domain.models.PutCounterOfferRequest
import com.proyectotitulo.appcomerciojcc.domain.models.ReviewState
import com.proyectotitulo.appcomerciojcc.domain.models.SelectedNegotiationOfferedCard
import com.proyectotitulo.appcomerciojcc.domain.models.SelectedNegotiationSolicitedCard
import com.proyectotitulo.appcomerciojcc.domain.models.TradeUiState
import com.proyectotitulo.appcomerciojcc.domain.models.WaitingRoomState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TradeViewModel : ViewModel() {

    private val repository = TradeRepository()
    private val matchmakingRepository = MatchmakingRepository()
    private val profileRepository = ProfileRepository()
    private val socketChatManager = SocketChatManager()

    private val _uiState = MutableLiveData<TradeUiState>(TradeUiState())
    val uiState: LiveData<TradeUiState> = _uiState

    private var pollingWaitingRoomJob: Job? = null

    init {
        setupGlobalSocket()
        loadActiveTransactions()
    }

    private fun setupGlobalSocket() {
        socketChatManager.ensureGlobalConnection(
            onTransactionStatusChanged = { txId, estado ->
                onSocketTransactionStatusChanged(txId, estado)
            }
        )
    }

    private fun onSocketTransactionStatusChanged(txId: Int, estado: String) {
        val currentState = _uiState.value ?: return
        val waiting = currentState.waitingRoomState

        if (estado == "COMPLETADA") {
            if (waiting.isOpen && waiting.transactionId == txId) {
                pollingWaitingRoomJob?.cancel()
                _uiState.value = currentState.copy(
                    waitingRoomState = WaitingRoomState(isOpen = false)
                )
                verifyCompletionAndCheckReview(txId, waiting.counterpartUsername, waiting.otherUserId)
            } else {
                loadActiveTransactions(forceRefresh = true)
            }
        }
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

    fun completeTransaction(transactionId: Int, counterpartUsername: String, counterpartUserId: Int) {
        setupGlobalSocket()

        viewModelScope.launch {
            val result = repository.completeTransaction(transactionId)

            result.onSuccess { response ->
                if (response.status == "success") {
                    val counterpartConfirmed = response.data?.counterpartConfirmed == true
                    val transactionState = response.data?.transactionState.orEmpty()
                    val otherUserId = counterpartUserId

                    val isFirstToConfirm = !counterpartConfirmed && transactionState != "COMPLETADA"

                    if (isFirstToConfirm) {
                        _uiState.value = (_uiState.value ?: TradeUiState()).copy(
                            waitingRoomState = WaitingRoomState(
                                isOpen = true,
                                transactionId = transactionId,
                                counterpartUsername = counterpartUsername,
                                otherUserId = otherUserId
                            )
                        )
                        startWaitingRoomPolling(transactionId, counterpartUsername, otherUserId)
                    } else {
                        pollingWaitingRoomJob?.cancel()
                        _uiState.value = (_uiState.value ?: TradeUiState()).copy(
                            waitingRoomState = WaitingRoomState(isOpen = false)
                        )
                        verifyCompletionAndCheckReview(transactionId, counterpartUsername, otherUserId)
                    }
                } else {
                    _uiState.value = (_uiState.value ?: TradeUiState()).copy(
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = (_uiState.value ?: TradeUiState()).copy(
                    errors = listOf(error.message ?: "Error al completar la transacción")
                )
            }
        }
    }

    private fun startWaitingRoomPolling(transactionId: Int, counterpartUsername: String, otherUserId: Int) {
        pollingWaitingRoomJob?.cancel()
        pollingWaitingRoomJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                val currentState = _uiState.value ?: break
                if (!currentState.waitingRoomState.isOpen || currentState.waitingRoomState.transactionId != transactionId) {
                    break
                }

                val txResult = repository.getTransactions(transactionId)
                txResult.onSuccess { response ->
                    val data = response.data
                    val state = data?.transactionState.orEmpty()
                    val confirmed = data?.counterpartConfirmed == true

                    if (state == "COMPLETADA" || confirmed) {
                        pollingWaitingRoomJob?.cancel()
                        _uiState.value = currentState.copy(
                            waitingRoomState = WaitingRoomState(isOpen = false)
                        )
                        verifyCompletionAndCheckReview(transactionId, counterpartUsername, otherUserId)
                    }
                }
            }
        }
    }

    fun withdrawCompleteTransaction() {
        pollingWaitingRoomJob?.cancel()
        val currentState = _uiState.value ?: return
        val waiting = currentState.waitingRoomState
        if (!waiting.isOpen) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                waitingRoomState = waiting.copy(isWithdrawing = true)
            )

            repository.withdrawCompleteTransaction(waiting.transactionId)

            val latest = _uiState.value ?: TradeUiState()
            _uiState.value = latest.copy(
                waitingRoomState = WaitingRoomState(isOpen = false)
            )
            loadActiveTransactions(forceRefresh = true)
        }
    }

    fun verifyCompletionAndCheckReview(transactionId: Int, counterpartUsername: String, otherUserId: Int) {
        viewModelScope.launch {
            val result = repository.getTransactions(transactionId)

            result.onSuccess { response ->
                if (response.status == "success") {
                    val details = response.data
                    val myReview = details?.myReview

                    if (myReview == null) {
                        _uiState.value = (_uiState.value ?: TradeUiState()).copy(
                            reviewState = ReviewState(
                                isOpen = true,
                                isAskToEditOpen = false,
                                transactionId = transactionId,
                                otherUserId = otherUserId,
                                counterpartUsername = counterpartUsername,
                                score = 5,
                                comment = "",
                                isEditingExisting = false
                            )
                        )
                    } else {
                        _uiState.value = (_uiState.value ?: TradeUiState()).copy(
                            reviewState = ReviewState(
                                isOpen = false,
                                isAskToEditOpen = true,
                                transactionId = transactionId,
                                otherUserId = otherUserId,
                                counterpartUsername = counterpartUsername,
                                score = myReview.score ?: 5,
                                comment = myReview.content.orEmpty(),
                                isEditingExisting = true
                            )
                        )
                    }
                }
                loadActiveTransactions(forceRefresh = true)
            }.onFailure {
                loadActiveTransactions(forceRefresh = true)
            }
        }
    }

    fun proceedToEditExistingReview() {
        val currentState = _uiState.value ?: return
        val review = currentState.reviewState

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                reviewState = review.copy(
                    isAskToEditOpen = false,
                    isOpen = true,
                    isLoadingReview = true
                )
            )

            val result = repository.getMyReview(review.otherUserId)
            val latest = _uiState.value ?: TradeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    val r = response.data
                    _uiState.value = latest.copy(
                        reviewState = latest.reviewState.copy(
                            score = r?.score ?: 5,
                            comment = r?.content.orEmpty(),
                            isLoadingReview = false
                        )
                    )
                } else {
                    _uiState.value = latest.copy(
                        reviewState = latest.reviewState.copy(isLoadingReview = false)
                    )
                }
            }.onFailure {
                _uiState.value = latest.copy(
                    reviewState = latest.reviewState.copy(isLoadingReview = false)
                )
            }
        }
    }

    fun updateReviewScore(score: Int) {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(
            reviewState = currentState.reviewState.copy(score = score.coerceIn(1, 5))
        )
    }

    fun updateReviewComment(comment: String) {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(
            reviewState = currentState.reviewState.copy(comment = comment.take(255))
        )
    }

    fun submitReview() {
        val currentState = _uiState.value ?: return
        val review = currentState.reviewState

        if (review.isSubmittingReview) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                reviewState = review.copy(isSubmittingReview = true, reviewError = null)
            )

            val result = repository.putReview(
                otherUserId = review.otherUserId,
                score = review.score,
                content = review.comment.trim()
            )

            val latest = _uiState.value ?: TradeUiState()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latest.copy(
                        reviewState = latest.reviewState.copy(
                            isSubmittingReview = false,
                            reviewSuccessMessage = response.message ?: "¡Reseña enviada con éxito!"
                        )
                    )
                    loadActiveTransactions(forceRefresh = true)
                } else {
                    _uiState.value = latest.copy(
                        reviewState = latest.reviewState.copy(
                            isSubmittingReview = false,
                            reviewError = response.errors?.firstOrNull() ?: response.message
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.value = latest.copy(
                    reviewState = latest.reviewState.copy(
                        isSubmittingReview = false,
                        reviewError = error.message ?: "Error al enviar la reseña"
                    )
                )
            }
        }
    }

    fun closeReviewState() {
        _uiState.value = (_uiState.value ?: TradeUiState()).copy(
            reviewState = ReviewState()
        )
        loadActiveTransactions(forceRefresh = true)
    }

    fun openPendingSummary(transactionId: Int) {
        val currentState = _uiState.value ?: TradeUiState()
        _uiState.value = currentState.copy(
            pendingSummaryState = PendingSummaryState(
                isOpen = true,
                transactionId = transactionId,
                isLoading = true
            )
        )

        viewModelScope.launch {
            val result = repository.getTransactions(transactionId)
            val latest = _uiState.value ?: TradeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latest.copy(
                        pendingSummaryState = latest.pendingSummaryState.copy(
                            details = response.data,
                            isLoading = false
                        )
                    )
                } else {
                    _uiState.value = latest.copy(
                        pendingSummaryState = latest.pendingSummaryState.copy(
                            isLoading = false,
                            error = response.errors?.firstOrNull() ?: response.message
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.value = latest.copy(
                    pendingSummaryState = latest.pendingSummaryState.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar resumen"
                    )
                )
            }
        }
    }

    fun closePendingSummary() {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(
            pendingSummaryState = PendingSummaryState(isOpen = false)
        )
    }

    fun openCancelConfirmation(transactionId: Int, counterpartUsername: String) {
        val currentState = _uiState.value ?: TradeUiState()
        _uiState.value = currentState.copy(
            cancelState = CancelConfirmationState(
                isOpen = true,
                transactionId = transactionId,
                counterpartUsername = counterpartUsername
            )
        )
    }

    fun confirmCancelTransaction() {
        val currentState = _uiState.value ?: return
        val cancel = currentState.cancelState
        val txId = cancel.transactionId

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                cancelState = cancel.copy(isCanceling = true, errorMessage = null)
            )

            val result = repository.cancelTransaction(txId)
            val latest = _uiState.value ?: TradeUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.value = latest.copy(
                        cancelState = latest.cancelState.copy(
                            isCanceling = false,
                            successMessage = response.message ?: "¡Transacción cancelada exitosamente!"
                        )
                    )
                    loadActiveTransactions(forceRefresh = true)
                } else {
                    _uiState.value = latest.copy(
                        cancelState = latest.cancelState.copy(
                            isCanceling = false,
                            errorMessage = response.errors?.firstOrNull() ?: response.message
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.value = latest.copy(
                    cancelState = latest.cancelState.copy(
                        isCanceling = false,
                        errorMessage = error.message ?: "Error al cancelar la transacción"
                    )
                )
            }
        }
    }

    fun closeCancelConfirmation() {
        val currentState = _uiState.value ?: return
        val wasSuccess = currentState.cancelState.successMessage != null

        _uiState.value = currentState.copy(
            cancelState = CancelConfirmationState(isOpen = false)
        )

        if (wasSuccess) {
            loadActiveTransactions(forceRefresh = true)
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

    // Funciones del Chat Socket.IO
    fun openChat(transactionId: Int, counterpartUsername: String, counterpartUserId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value ?: TradeUiState()

            val profileResult = profileRepository.getPrivateProfile()
            var myUserId = 0
            profileResult.onSuccess { response ->
                if (response.status == "success") {
                    myUserId = response.data?.userId ?: 0
                }
            }

            _uiState.value = currentState.copy(
                chatState = ChatState(
                    isOpen = true,
                    transactionId = transactionId,
                    counterpartUsername = counterpartUsername,
                    counterpartUserId = counterpartUserId,
                    currentUserId = myUserId,
                    isLoadingHistory = true,
                    chatError = null
                )
            )

            val historyResult = repository.getMessageHistory(transactionId)
            var historyMessages: List<GetMessageHistoryResponse.Message> = emptyList()

            historyResult.onSuccess { response ->
                if (response.status == "success") {
                    historyMessages = response.data ?: emptyList()
                }
            }

            val latest = _uiState.value ?: TradeUiState()
            _uiState.value = latest.copy(
                chatState = latest.chatState.copy(
                    messages = historyMessages,
                    isLoadingHistory = false
                )
            )

            socketChatManager.setListeners(
                onNewMessage = { newMsg ->
                    onNewSocketMessageReceived(newMsg)
                },
                onMessagesRead = { hastaMsgId ->
                    onSocketMessagesRead(hastaMsgId)
                },
                onChatClosed = {
                    onSocketChatClosed()
                },
                onTransactionStatusChanged = { txId, estado ->
                    onSocketTransactionStatusChanged(txId, estado)
                }
            )

            socketChatManager.joinChatRoom(
                transactionId = transactionId,
                onConnected = {
                    val unreadFromOther = historyMessages.filter { it.senderId != myUserId && it.isRead != true }
                    val maxUnreadId = unreadFromOther.mapNotNull { it.messageId }.maxOrNull()
                    if (maxUnreadId != null) {
                        socketChatManager.markAsRead(transactionId, maxUnreadId)
                    }
                }
            )
        }
    }

    private fun onNewSocketMessageReceived(newMsg: GetMessageHistoryResponse.Message) {
        val currentState = _uiState.value ?: return
        val chat = currentState.chatState

        val isDuplicate = chat.messages.any { existing ->
            (newMsg.messageId != null && existing.messageId == newMsg.messageId) ||
            (newMsg.messageId == null && existing.senderId == newMsg.senderId && existing.content == newMsg.content)
        }

        if (isDuplicate) return

        val updatedMessages = chat.messages + newMsg

        _uiState.value = currentState.copy(
            chatState = chat.copy(messages = updatedMessages)
        )

        if (newMsg.senderId != chat.currentUserId && newMsg.messageId != null) {
            socketChatManager.markAsRead(chat.transactionId, newMsg.messageId)
        }
    }

    private fun onSocketMessagesRead(hastaMsgId: Int) {
        val currentState = _uiState.value ?: return
        val chat = currentState.chatState

        val updatedMessages = chat.messages.map { msg ->
            if (msg.senderId == chat.currentUserId && (msg.messageId ?: 0) <= hastaMsgId) {
                msg.copy(isRead = true)
            } else {
                msg
            }
        }

        _uiState.value = currentState.copy(
            chatState = chat.copy(messages = updatedMessages)
        )
    }

    private fun onSocketChatClosed() {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(
            chatState = currentState.chatState.copy(isChatClosed = true)
        )
    }

    fun updateChatInput(newInput: String) {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(
            chatState = currentState.chatState.copy(inputContent = newInput)
        )
    }

    fun sendMessage() {
        val currentState = _uiState.value ?: return
        val chat = currentState.chatState
        val content = chat.inputContent.trim()

        if (content.length !in 1..255 || chat.isSending || chat.isChatClosed) return

        _uiState.value = currentState.copy(
            chatState = chat.copy(isSending = true, chatError = null)
        )

        socketChatManager.sendMessage(
            transactionId = chat.transactionId,
            content = content,
            onSuccess = { sentMessage ->
                val latest = _uiState.value ?: return@sendMessage
                val currentMsgs = latest.chatState.messages

                val isDuplicate = sentMessage != null && currentMsgs.any { existing ->
                    (sentMessage.messageId != null && existing.messageId == sentMessage.messageId) ||
                    (existing.senderId == sentMessage.senderId && existing.content == sentMessage.content)
                }

                val updatedMsgs = if (sentMessage != null && !isDuplicate) {
                    currentMsgs + sentMessage
                } else {
                    currentMsgs
                }

                _uiState.value = latest.copy(
                    chatState = latest.chatState.copy(
                        messages = updatedMsgs,
                        inputContent = "",
                        isSending = false
                    )
                )
            },
            onError = { error ->
                val latest = _uiState.value ?: return@sendMessage
                _uiState.value = latest.copy(
                    chatState = latest.chatState.copy(
                        isSending = false,
                        chatError = error
                    )
                )
            }
        )
    }

    fun closeChat() {
        val currentState = _uiState.value ?: return
        val txId = currentState.chatState.transactionId

        socketChatManager.leaveRoom(txId)

        _uiState.value = currentState.copy(
            chatState = ChatState(isOpen = false)
        )
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
        pollingWaitingRoomJob?.cancel()
        socketChatManager.disconnect()
        _uiState.value = TradeUiState()
    }

    override fun onCleared() {
        super.onCleared()
        pollingWaitingRoomJob?.cancel()
        socketChatManager.disconnect()
    }
}
