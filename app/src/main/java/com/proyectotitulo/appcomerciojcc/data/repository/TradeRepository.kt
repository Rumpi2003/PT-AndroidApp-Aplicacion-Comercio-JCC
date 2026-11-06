package com.proyectotitulo.appcomerciojcc.data.repository

import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.domain.models.CancelTransactionResponse
import com.proyectotitulo.appcomerciojcc.domain.models.CompleteTransactionResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetMessageHistoryResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetMyReviewResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTcgsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTransactionResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTransactionsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.PatchTransactionDecisionResponse
import com.proyectotitulo.appcomerciojcc.domain.models.PutCounterOfferRequest
import com.proyectotitulo.appcomerciojcc.domain.models.PutCounterOfferRequest.*
import com.proyectotitulo.appcomerciojcc.domain.models.PutCounterOfferResponse
import com.proyectotitulo.appcomerciojcc.domain.models.PutReviewRequest
import com.proyectotitulo.appcomerciojcc.domain.models.PutReviewResponse
import com.proyectotitulo.appcomerciojcc.domain.models.WithdrawCompleteTransactionResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TradeRepository {
    private val client = KtorApiClient.httpClient

    suspend fun getTransactions(
        view: String = "activas",
        state: String? = null,
        limit: Int = 10,
        offset: Int = 0
    ): Result<GetTransactionsResponse> {
        return try {
            val response: GetTransactionsResponse = client.get("${KtorApiClient.BASE_URL}/transacciones/") {
                parameter("vista", view)
                parameter("estado", state)
                parameter("limite", limit)
                parameter("offset", offset)
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getTransactions(transactionId: Int): Result<GetTransactionResponse> {
        return try {
            val response: GetTransactionResponse = client.get("${KtorApiClient.BASE_URL}/transacciones/${transactionId}").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun putCounterOffer(
        transactionId: Int,
        solicitedCards: List<Card>,
        offeredCards: List<Card>
    ): Result<PutCounterOfferResponse> {
        return try {
            val response: PutCounterOfferResponse = client.put("${KtorApiClient.BASE_URL}/transacciones/${transactionId}/contraoferta") {
                contentType(ContentType.Application.Json)
                setBody(
                    PutCounterOfferRequest(
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

    suspend fun acceptTransaction(transactionId: Int): Result<PatchTransactionDecisionResponse> {
        return try {
            val response: PatchTransactionDecisionResponse = client.patch("${KtorApiClient.BASE_URL}/transacciones/${transactionId}/aceptar").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun declineTransaction(transactionId: Int): Result<PatchTransactionDecisionResponse> {
        return try {
            val response: PatchTransactionDecisionResponse = client.patch("${KtorApiClient.BASE_URL}/transacciones/${transactionId}/rechazar").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getMessageHistory(
        transactionId: Int,
        before: Int? = null,
        after: Int? = null,
        limit: Int = 50
    ): Result<GetMessageHistoryResponse> {
        return try {
            val response: GetMessageHistoryResponse = client.get("${KtorApiClient.BASE_URL}/transacciones/${transactionId}/mensajes") {
                parameter("antes", before)
                parameter("despues", after)
                parameter("limite", limit)
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun cancelTransaction(transactionId: Int): Result<CancelTransactionResponse> {
        return try {
            val response: CancelTransactionResponse = client.patch("${KtorApiClient.BASE_URL}/transacciones/${transactionId}/cancelar").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun completeTransaction(transactionId: Int): Result<CompleteTransactionResponse> {
        return try {
            val response: CompleteTransactionResponse = client.patch("${KtorApiClient.BASE_URL}/transacciones/${transactionId}/completar").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun withdrawCompleteTransaction(transactionId: Int): Result<WithdrawCompleteTransactionResponse> {
        return try {
            val response: WithdrawCompleteTransactionResponse = client.delete("${KtorApiClient.BASE_URL}/transacciones/${transactionId}/completar").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getMyReview(otherUserId: Int): Result<GetMyReviewResponse> {
        return try {
            val response: GetMyReviewResponse = client.get("${KtorApiClient.BASE_URL}/resenas/${otherUserId}/mia").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun putReview(
        otherUserId: Int,
        score: Int,
        content: String
    ): Result<PutReviewResponse> {
        return try {
            val response: PutReviewResponse = client.put("${KtorApiClient.BASE_URL}/resenas/${otherUserId}") {
                contentType(ContentType.Application.Json)
                setBody(
                    PutReviewRequest(
                        score = score,
                        content = content
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