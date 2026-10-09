package com.proyectotitulo.appcomerciojcc.data.repository

import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.domain.models.AddInventoryCardRequest
import com.proyectotitulo.appcomerciojcc.domain.models.AddInventoryCardResponse
import com.proyectotitulo.appcomerciojcc.domain.models.AddWishlistCardRequest
import com.proyectotitulo.appcomerciojcc.domain.models.AddWishlistCardResponse
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class InventoryRepository {
    private val client = KtorApiClient.httpClient

    suspend fun addInventoryCard(cardId: Int, condition: String, language: String,
                                 price: Int, quantity: Int): Result<AddInventoryCardResponse> {
        return try {
            val response: AddInventoryCardResponse = client.post("${KtorApiClient.BASE_URL}/inventario/ofrecidas") {
                contentType(ContentType.Application.Json)
                setBody(AddInventoryCardRequest(
                    cardId = cardId,
                    condition = condition,
                    language = language,
                    price = price,
                    quantity = quantity
                ))
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun addWishlistCard(cardId: Int, minimumCondition: String, language: String, quantity: Int): Result<AddWishlistCardResponse> {
        return try {
            val response: AddWishlistCardResponse = client.post("${KtorApiClient.BASE_URL}/inventario/deseadas") {
                contentType(ContentType.Application.Json)
                setBody(
                    AddWishlistCardRequest(
                        cardId = cardId,
                        minimumCondition = minimumCondition,
                        language = language,
                        quantity = quantity
                    )
                )
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}