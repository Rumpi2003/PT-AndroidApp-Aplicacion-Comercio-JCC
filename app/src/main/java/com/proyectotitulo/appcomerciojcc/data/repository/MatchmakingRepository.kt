package com.proyectotitulo.appcomerciojcc.data.repository

import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.domain.models.GetGeoModeResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetMatchmakingResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetMostPopularCardsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTradeInventoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.PostTransactionRequest
import com.proyectotitulo.appcomerciojcc.domain.models.PostTransactionRequest.SolicitedCard
import com.proyectotitulo.appcomerciojcc.domain.models.PostTransactionRequest.OfferedCard
import com.proyectotitulo.appcomerciojcc.domain.models.PostTransactionResponse
import com.proyectotitulo.appcomerciojcc.domain.models.UpdateGeoModeRequest
import com.proyectotitulo.appcomerciojcc.domain.models.UpdateGeoModeResponse
import com.proyectotitulo.appcomerciojcc.domain.models.UpdateLocationRequest
import com.proyectotitulo.appcomerciojcc.domain.models.UpdateLocationResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class MatchmakingRepository {
    private val client = KtorApiClient.httpClient

    suspend fun getMatchmaking(
        limit: Int = 5,
        offset: Int = 0,
        useGeolocation: Boolean = false,
        userCommune: Boolean = false
    ): Result<GetMatchmakingResponse> {
        return try {
            val response: GetMatchmakingResponse = client.get("${KtorApiClient.BASE_URL}/emparejamientos") {
                parameter("limite", limit)
                parameter("offset", offset)
                parameter("usarGeolocalizacion", useGeolocation)
                parameter("soloMiComuna", userCommune)
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getGeoMode(): Result<GetGeoModeResponse> {
        return try {
            val response: GetGeoModeResponse = client.get("${KtorApiClient.BASE_URL}/usuarios/modo_geo").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateGeoMode(geoMode: Boolean): Result<UpdateGeoModeResponse> {
        return try {
            val response: UpdateGeoModeResponse = client.put("${KtorApiClient.BASE_URL}/usuarios/modo_geo") {
                contentType(ContentType.Application.Json)
                setBody(
                    UpdateGeoModeRequest(
                        geoMode = geoMode
                    )
                )
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateLocation(latitude: Float, longitude: Float): Result<UpdateLocationResponse> {
        return try {
            val response: UpdateLocationResponse = client.put("${KtorApiClient.BASE_URL}/usuarios/ubicacion") {
                contentType(ContentType.Application.Json)
                setBody(
                    UpdateLocationRequest(
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getOtherInventory(otherUserId: Int): Result<GetTradeInventoryResponse> {
        return try {
            val response: GetTradeInventoryResponse = client.get("${KtorApiClient.BASE_URL}/transacciones/contraparte/${otherUserId}/inventario").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getMyInventory(otherUserId: Int): Result<GetTradeInventoryResponse> {
        return try {
            val response: GetTradeInventoryResponse = client.get("${KtorApiClient.BASE_URL}/transacciones/contraparte/${otherUserId}/mi-inventario").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun postTransaction(
        otherUserId: Int,
        solicitedCards: List<SolicitedCard>,
        offeredCards: List<OfferedCard>
    ): Result<PostTransactionResponse> {
        return try {
            val response: PostTransactionResponse = client.post("${KtorApiClient.BASE_URL}/transacciones/") {
                contentType(ContentType.Application.Json)
                setBody(
                    PostTransactionRequest(
                        otherUserId = otherUserId,
                        solicitedCards = solicitedCards,
                        offeredCards = offeredCards
                    )
                )
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getMostPopularCards(): Result<GetMostPopularCardsResponse> {
        return try {
            val response: GetMostPopularCardsResponse = client.get("${KtorApiClient.BASE_URL}/popularidad/").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
