package com.proyectotitulo.appcomerciojcc.domain.models

import com.proyectotitulo.appcomerciojcc.domain.models.PostTransactionResponse.Data
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMatchmakingRequest(
    @SerialName("usarGeolocalizacion")
    val useGeolocation: Boolean? = false,
    @SerialName("limite")
    val limit: Int? = 5,
    @SerialName("offset")
    val offset: Int? = 1
)

@Serializable
data class GetMatchmakingResponse(
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
            @SerialName("usuario")
            val user: User? = null,
            @SerialName("puntuacion")
            val matchmakingScore: Float? = null,
            @SerialName("distancia_m")
            val distance: Int? = null,
            @SerialName("distancia_aproximada")
            val distanceAprox: Boolean? = null,
            @SerialName("cartas_coincidentes")
            val matchingCards: List<Card>? = null,
            @SerialName("resumen")
            val summary: Summary? = null,
            @SerialName("desglose")
            val breakdown: Breakdown? = null
        ) {
            @Serializable
            data class User(
                @SerialName("id_usuario")
                val userId: Int? = null,
                @SerialName("nombre_usuario")
                val username: String? = null,
                @SerialName("descripcion_perfil")
                val profileDescription: String? = null,
                @SerialName("puntuacion_promedio")
                val averageScore: Float? = null,
                @SerialName("comuna")
                val commune: String? = null
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
                @SerialName("idioma")
                val language: String? = null,
                @SerialName("estado")
                val condition: String? = null,
                @SerialName("precio")
                val price: Int? = null,
                @SerialName("cantidad")
                val quantity: Int? = null
            )

            @Serializable
            data class Summary(
                @SerialName("unidades_cubiertas")
                val coveredUnits: Int? = null,
                @SerialName("unidades_deseadas")
                val wantedUnits: Int? = null,
                @SerialName("porcentaje_cobertura")
                val coveragePercentage: Float? = null,
                @SerialName("unidades_reciprocas")
                val reciprocatedUnits: Int? = null
            )

            @Serializable
            data class Breakdown(
                @SerialName("cobertura")
                val coverage: Float? = null,
                @SerialName("reciprocidad")
                val reciprocity: Float? = null,
                @SerialName("precio")
                val price: Float? = null,
                @SerialName("distancia")
                val distance: Float? = null,
                @SerialName("reputacion")
                val reputation: Float? = null,
                @SerialName("estado")
                val condition: Float? = null
            )
        }
    }
}

@Serializable
data class GetGeoModeResponse(
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
        @SerialName("modo_geo_activado")
        val geoMode: Boolean? = null
    )
}

@Serializable
data class UpdateGeoModeRequest(
    @SerialName("modo_geo_activado")
    val geoMode: Boolean
)

@Serializable
data class UpdateGeoModeResponse(
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
        @SerialName("modo_geo_activado")
        val geoMode: Boolean? = null
    )
}

@Serializable
data class UpdateLocationRequest(
    @SerialName("latitud")
    val latitude: Float,
    @SerialName("longitud")
    val longitude: Float
)

@Serializable
data class UpdateLocationResponse(
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
        @SerialName("latitud")
        val latitude: Float? = null,
        @SerialName("longitud")
        val longitude: Float? = null
    )
}

@Serializable
data class GetTradeInventoryResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: List<Card>? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Card(
        @SerialName("id_carta_ofrecida")
        val inventoryCardId: Int? = null,
        @SerialName("id_tipo_carta")
        val cardId: Int? = null,
        @SerialName("nombre_carta")
        val cardName: String? = null,
        @SerialName("rareza")
        val rarity: String? = null,
        @SerialName("set")
        val set: String? = null,
        @SerialName("url_imagen")
        val imageUrl: String? = null,
        @SerialName("url_miniatura")
        val miniatureUrl: String? = null,
        @SerialName("franquicia")
        val tcg: String? = null,
        @SerialName("estado")
        val condition: String? = null,
        @SerialName("idioma")
        val language: String? = null,
        @SerialName("precio")
        val price: Int? = null,
        @SerialName("stock_disponible")
        val availableStock: Int? = null,
        @SerialName("en_lista_deseos")
        val inWishlist: Boolean? = null,
        @SerialName("cantidad_deseada")
        val desiredAmount: Int? = null
    )
}

@Serializable
data class PostTransactionRequest(
    @SerialName("id_usuario_destino")
    val otherUserId: Int,
    @SerialName("solicitadas")
    val solicitedCards: List<SolicitedCard>,
    @SerialName("ofrecidas")
    val offeredCards: List<OfferedCard>
) {
    @Serializable
    data class SolicitedCard(
        @SerialName("id_carta_ofrecida")
        val inventoryCardId: Int,
        @SerialName("cantidad")
        val quantity: Int,
        @SerialName("precio_ofertado")
        val offeredPrice: Int
    )

    @Serializable
    data class OfferedCard(
        @SerialName("id_carta_ofrecida")
        val inventoryCardId: Int,
        @SerialName("cantidad")
        val quantity: Int
    )
}

@Serializable
data class PostTransactionResponse(
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
        @SerialName("id_usuario_1")
        val userId: Int? = null,
        @SerialName("id_usuario_2")
        val otherUserId: Int? = null,
        @SerialName("estado")
        val transactionState: String? = null,
        @SerialName("fecha_creacion")
        val creationDate: String? = null,
        @SerialName("diferencia_a_pagar_1")
        val userDifferential: Int? = null,
        @SerialName("diferencia_a_pagar_2")
        val otherUserDifferential: Int? = null,
        @SerialName("deudor")
        val debtorId: Int? = null,
        @SerialName("solicitadas")
        val solicitedCards: List<SolicitedCard>? = null,
        @SerialName("ofrecidas")
        val offeredCards: List<OfferedCard>? = null
    ) {
        @Serializable
        data class SolicitedCard(
            @SerialName("id_carta_ofrecida")
            val inventoryCardId: Int? = null,
            @SerialName("cantidad")
            val quantity: Int? = null,
            @SerialName("precio_acordado")
            val agreedUponPrice: Int? = null
        )

        @Serializable
        data class OfferedCard(
            @SerialName("id_carta_ofrecida")
            val inventoryCardId: Int? = null,
            @SerialName("cantidad")
            val quantity: Int? = null,
            @SerialName("precio_acordado")
            val agreedUponPrice: Int? = null
        )
    }
}

@Serializable
data class GetMostPopularCardsResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: List<Card>? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Card(
        @SerialName("id_tipo_carta")
        val cardId: Int? = null,
        @SerialName("nombre_carta")
        val cardName: String? = null,
        @SerialName("set")
        val set: String? = null,
        @SerialName("rareza")
        val rarity: String? = null,
        @SerialName("url_imagen")
        val imageUrl: String? = null,
        @SerialName("url_miniatura")
        val miniatureUrl: String? = null,
        @SerialName("transacciones_completadas")
        val completedTransactions: Int? = null,
        @SerialName("usuarios_que_la_desean")
        val wantedByUsers: Int? = null,
        @SerialName("puntaje")
        val score: Int? = null
    )
}

data class SelectedSolicitedCard(
    val card: GetTradeInventoryResponse.Card,
    val quantity: Int = 1,
    val offeredPrice: Int = card.price ?: 0
)

data class SelectedOfferedCard(
    val card: GetTradeInventoryResponse.Card,
    val quantity: Int = 1
)

data class TransactionState(
    val isOpen: Boolean = false,
    val step: Int = 1, // 1 = Solicitadas, 2 = Ofrecidas, 3 = Resumen, 4 = Resultado
    val targetUser: GetMatchmakingResponse.Data.Result.User? = null,
    val otherUserInventory: List<GetTradeInventoryResponse.Card> = emptyList(),
    val myInventory: List<GetTradeInventoryResponse.Card> = emptyList(),
    val selectedSolicitedCards: Map<Int, SelectedSolicitedCard> = emptyMap(),
    val selectedOfferedCards: Map<Int, SelectedOfferedCard> = emptyMap(),
    val isLoadingInventories: Boolean = false,
    val isSubmittingTransaction: Boolean = false,
    val transactionSuccessMessage: String? = null,
    val transactionError: String? = null
)

data class HomeUiState(
    val isAllOffersMode: Boolean = false,
    val recommendations: List<GetMatchmakingResponse.Data.Result> = emptyList(),
    val totalRecommendations: Int = 0,
    val currentPage: Int = 1,
    val limitPerPage: Int = 5,
    val useGeolocation: Boolean = false,
    val useUserCommune: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val isLoadingGeoMode: Boolean = false,
    val isRefreshing: Boolean = false,
    val errors: List<String>? = null,
    val transactionState: TransactionState = TransactionState(),
    val popularCards: List<GetMostPopularCardsResponse.Card> = emptyList(),
    val isLoadingPopularCards: Boolean = false
)
