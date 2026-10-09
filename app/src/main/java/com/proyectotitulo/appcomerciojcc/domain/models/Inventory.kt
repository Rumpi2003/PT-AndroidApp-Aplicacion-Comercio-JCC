package com.proyectotitulo.appcomerciojcc.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val data: InventoryCard? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class InventoryCard(
        @SerialName("id_carta_ofrecida")
        val inventoryCardId: Int?,
        @SerialName("id_tipo_carta")
        val cardId: Int?,
        @SerialName("estado")
        val condition: String?,
        @SerialName("idioma")
        val language: String?,
        @SerialName("precio")
        val price: Int?,
        @SerialName("cantidad")
        val quantity: Int?
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
    val data: WishlistCard? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class WishlistCard(
        @SerialName("id_carta_deseada")
        val wishlistCardId: Int?,
        @SerialName("id_tipo_carta")
        val cardId: Int?,
        @SerialName("estado_minimo")
        val minimumCondition: String?,
        @SerialName("idioma")
        val language: String?,
        @SerialName("cantidad")
        val quantity: Int?
    )
}