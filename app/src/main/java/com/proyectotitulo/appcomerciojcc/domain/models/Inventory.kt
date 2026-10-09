package com.proyectotitulo.appcomerciojcc.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AddInventoryCardRequest(
    @SerialName("id_tipo_carta")
    val cardId: Int,
    @SerialName("estado")
    val condition: String,
    @SerialName("idioma")
    val language: String,
    @SerialName("precio")
    val price: Int,
    @SerialName("cantidad")
    val quantity: Int
)

@Serializable
data class AddInventoryCardResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: JsonElement? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class InventoryCard(
        @SerialName("id_carta_ofrecida")
        val inventoryCardId: Int? = null,
        @SerialName("id_tipo_carta")
        val cardId: Int? = null,
        @SerialName("estado")
        val condition: String? = null,
        @SerialName("idioma")
        val language: String? = null,
        @SerialName("precio")
        val price: Int? = null,
        @SerialName("cantidad")
        val quantity: Int? = null
    )
}

@Serializable
data class AddWishlistCardRequest(
    @SerialName("id_tipo_carta")
    val cardId: Int,
    @SerialName("estado_minimo")
    val minimumCondition: String,
    @SerialName("idioma")
    val language: String,
    @SerialName("cantidad")
    val quantity: Int
)

@Serializable
data class AddWishlistCardResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: JsonElement? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class WishlistCard(
        @SerialName("id_carta_deseada")
        val wishlistCardId: Int? = null,
        @SerialName("id_tipo_carta")
        val cardId: Int? = null,
        @SerialName("estado_minimo")
        val minimumCondition: String? = null,
        @SerialName("idioma")
        val language: String? = null,
        @SerialName("cantidad")
        val quantity: Int? = null
    )
}

@Serializable
data class GetInventoryResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: List<InventoryCard>? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class InventoryCard(
        @SerialName("id_carta_ofrecida")
        val inventoryCardId: Int? = null,
        @SerialName("id_tipo_carta")
        val cardId: Int? = null,
        @SerialName("nombre_carta")
        val cardName: String? = null,
        @SerialName("rareza")
        val cardRarity: String? = null,
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
        @SerialName("cantidad")
        val quantity: Int? = null
    )
}

@Serializable
data class GetWishlistResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: List<WishlistCard>? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class WishlistCard(
        @SerialName("id_carta_deseada")
        val wishlistCardId: Int? = null,
        @SerialName("id_tipo_carta")
        val cardId: Int? = null,
        @SerialName("nombre_carta")
        val cardName: String? = null,
        @SerialName("rareza")
        val cardRarity: String? = null,
        @SerialName("set")
        val set: String? = null,
        @SerialName("url_imagen")
        val imageUrl: String? = null,
        @SerialName("url_miniatura")
        val miniatureUrl: String? = null,
        @SerialName("franquicia")
        val tcg: String? = null,
        @SerialName("estado_minimo")
        val minimumCondition: String? = null,
        @SerialName("idioma")
        val language: String? = null,
        @SerialName("cantidad")
        val quantity: Int? = null
    )
}

data class InventoryUiState(
    val selectedTab: Int = 0, // 0 = Inventario (Ofrecidas), 1 = Lista de Deseos (Deseadas)
    val inventoryCards: List<GetInventoryResponse.InventoryCard> = emptyList(),
    val wishlistCards: List<GetWishlistResponse.WishlistCard> = emptyList(),
    val isGridView: Boolean = true,
    val isLoadingInventory: Boolean = false,
    val isLoadingWishlist: Boolean = false,
    val errors: List<String>? = null
)