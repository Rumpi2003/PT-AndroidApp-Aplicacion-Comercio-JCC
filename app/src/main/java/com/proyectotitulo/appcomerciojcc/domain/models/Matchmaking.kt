package com.proyectotitulo.appcomerciojcc.domain.models

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

data class HomeUiState(
    val isAllOffersMode: Boolean = false,
    val recommendations: List<GetMatchmakingResponse.Data.Result> = emptyList(),
    val totalRecommendations: Int = 0,
    val currentPage: Int = 1,
    val limitPerPage: Int = 5,
    val useGeolocation: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val isLoadingGeoMode: Boolean = false,
    val isRefreshing: Boolean = false,
    val errors: List<String>? = null
)
