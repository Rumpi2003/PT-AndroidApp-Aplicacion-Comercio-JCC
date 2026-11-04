package com.proyectotitulo.appcomerciojcc.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetTransactionsResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Data? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Data(
        @SerialName("total")
        val total: Int? = null,
        @SerialName("resultados")
        val results: List<Result>? = null
    ) {
        @Serializable
        data class Result(
            @SerialName("id_transaccion")
            val transactionId: Int? = null,
            @SerialName("estado")
            val transactionState: String? = null,
            @SerialName("fecha_creacion")
            val creationDate: String? = null,
            @SerialName("fecha_estado")
            val stateDate: String? = null,
            @SerialName("mi_turno")
            val myTurn: Boolean? = null,
            @SerialName("rol")
            val role: String? = null,
            @SerialName("contraparte")
            val counterpart: Counterpart? = null,
            @SerialName("resumen")
            val summary: Summary? = null
        ) {
            @Serializable
            data class Counterpart(
                @SerialName("id_usuario")
                val userId: Int? = null,
                @SerialName("nombre_usuario")
                val username: String? = null
            )

            @Serializable
            data class Summary(
                @SerialName("unidades_recibo")
                val receivedUnits: Int? = null,
                @SerialName("unidades_entrego")
                val deliveredUnits: Int? = null,
                @SerialName("valor_recibo")
                val receivedValue: Int? = null,
                @SerialName("valor_entrego")
                val deliveredValue: Int? = null,
                @SerialName("mi_diferencia_a_pagar")
                val myDebt: Int? = null,
                @SerialName("diferencia_a_pagar_contraparte")
                val counterpartDebt: Int? = null,
                @SerialName("deudor")
                val debtor: String? = null
            )
        }
    }
}

@Serializable
data class GetTransactionResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Data? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Data(
        @SerialName("id_transaccion")
        val transactionId: Int? = null,
        @SerialName("estado")
        val transactionState: String? = null,
        @SerialName("fecha_creacion")
        val creationDate: String? = null,
        @SerialName("fecha_estado")
        val stateDate: String? = null,
        @SerialName("mi_turno")
        val myTurn: Boolean? = null,
        @SerialName("rol")
        val role: String? = null,
        @SerialName("contraparte")
        val counterpart: Counterpart? = null,
        @SerialName("resumen")
        val summary: Summary? = null,
        @SerialName("cartas")
        val cards: List<Card>? = null
    ) {
        @Serializable
        data class Counterpart(
            @SerialName("id_usuario")
            val userId: Int? = null,
            @SerialName("nombre_usuario")
            val username: String? = null
        )

        @Serializable
        data class Summary(
            @SerialName("unidades_recibo")
            val receivedUnits: Int? = null,
            @SerialName("unidades_entrego")
            val deliveredUnits: Int? = null,
            @SerialName("valor_recibo")
            val receivedValue: Int? = null,
            @SerialName("valor_entrego")
            val deliveredValue: Int? = null,
            @SerialName("mi_diferencia_a_pagar")
            val myDebt: Int? = null,
            @SerialName("diferencia_a_pagar_contraparte")
            val counterpartDebt: Int? = null,
            @SerialName("deudor")
            val debtor: String? = null
        )

        @Serializable
        data class Card(
            @SerialName("id_carta_ofrecida")
            val inventoryCardId: Int? = null,
            @SerialName("id_tipo_carta")
            val cardId: Int? = null,
            @SerialName("nombre_carta")
            val cardName: String? = null,
            @SerialName("set")
            val set: String? = null,
            @SerialName("url_imagen")
            val imageUrl: String? = null,
            @SerialName("url_miniatura")
            val miniatureUrl: String? = null,
            @SerialName("estado")
            val condition: String? = null,
            @SerialName("idioma")
            val language: String? = null,
            @SerialName("cantidad")
            val quantity: Int? = null,
            @SerialName("precio_acordado")
            val agreedUponPrice: Int? = null,
            @SerialName("sentido")
            val direction: String? = null // "ENTREGO" o "RECIBO"
        )
    }
}

@Serializable
data class PutCounterOfferRequest(
    @SerialName("solicitadas")
    val solicitedCards: List<Card>,
    @SerialName("ofrecidas")
    val offeredCards: List<Card>
) {
    @Serializable
    data class Card(
        @SerialName("id_carta_ofrecida")
        val cardId: Int,
        @SerialName("cantidad")
        val quantity: Int,
        @SerialName("precio_ofertado")
        val offeredPrice: Int
    )
}

@Serializable
data class PutCounterOfferResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Data? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Data(
        @SerialName("id_transaccion")
        val transactionId: Int? = null,
        @SerialName("estado")
        val transactionState: String? = null,
        @SerialName("id_usuario_turno")
        val turnUserId: Int? = null,
        @SerialName("deferencia_a_pagar_1")
        val user1Differential: Int? = null,
        @SerialName("deferencia_a_pagar_2")
        val user2Differential: Int? = null
    )
}

@Serializable
data class PatchTransactionDecisionResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Data? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Data(
        @SerialName("id_transaccion")
        val transactionId: Int? = null,
        @SerialName("estado")
        val transactionState: String? = null
    )
}

data class SelectedNegotiationSolicitedCard(
    val card: GetTradeInventoryResponse.Card,
    val quantity: Int = 1,
    val offeredPrice: Int = card.price ?: 0
)

data class SelectedNegotiationOfferedCard(
    val card: GetTradeInventoryResponse.Card,
    val quantity: Int = 1,
    val offeredPrice: Int = card.price ?: 0
)

data class NegotiationState(
    val isOpen: Boolean = false,
    val step: Int = 3, // 3 = Resumen, 1 = Cartas Solicitadas, 2 = Cartas Ofrecidas, 4 = Resultado
    val transactionId: Int = 0,
    val targetUser: GetTransactionsResponse.Data.Result.Counterpart? = null,
    val initialTransactionDetails: GetTransactionResponse.Data? = null,
    val otherUserInventory: List<GetTradeInventoryResponse.Card> = emptyList(),
    val myInventory: List<GetTradeInventoryResponse.Card> = emptyList(),
    val selectedSolicitedCards: Map<Int, SelectedNegotiationSolicitedCard> = emptyMap(),
    val selectedOfferedCards: Map<Int, SelectedNegotiationOfferedCard> = emptyMap(),
    val hasModifications: Boolean = false,
    val isLoadingDetails: Boolean = false,
    val isSubmittingAction: Boolean = false,
    val negotiationSuccessMessage: String? = null,
    val negotiationError: String? = null
)

data class TradeUiState(
    val isHistoryView: Boolean = false,

    val pendingTransactions: List<GetTransactionsResponse.Data.Result> = emptyList(), // Max 5
    val myNegotiatingTransactions: List<GetTransactionsResponse.Data.Result> = emptyList(), // PROPONENTE (Max 10)
    val receivedNegotiatingTransactions: List<GetTransactionsResponse.Data.Result> = emptyList(), // DESTINATARIO (Unlimited, paginated)
    val receivedTotal: Int = 0,
    val isLoadingMoreReceived: Boolean = false,

    val historyTransactions: List<GetTransactionsResponse.Data.Result> = emptyList(),
    val historyTotal: Int = 0,
    val selectedHistoryFilter: String? = null, // null = Todas, "COMPLETADA", "CANCELADA", "RECHAZADA"
    val isLoadingMoreHistory: Boolean = false,

    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errors: List<String>? = null,

    val negotiationState: NegotiationState = NegotiationState()
)
