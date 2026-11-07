package com.proyectotitulo.appcomerciojcc.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object RegisterRoute

// Rutas de la barra de navegación
@Serializable
object InventoryRoute

@Serializable
object SearchRoute

@Serializable
object HomeRoute

@Serializable
object TradeRoute

@Serializable
object ProfileRoute

@Serializable
data class PublicProfileRoute(val userId: Int)

@Serializable
data class ReviewsRoute(val userId: Int, val username: String)
