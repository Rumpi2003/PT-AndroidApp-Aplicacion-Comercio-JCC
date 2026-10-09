package com.proyectotitulo.appcomerciojcc.data.repository

import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.domain.models.GetCardsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetSetsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetTcgsResponse
import com.proyectotitulo.appcomerciojcc.domain.models.RegisterResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class SearchRepository {
    private val client = KtorApiClient.httpClient

    suspend fun getTcgs(): Result<GetTcgsResponse> {
        return try {
            val response: GetTcgsResponse = client.get("${KtorApiClient.BASE_URL}/tcgApi/listar_tcgs").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getSets(tcgId: String, sortBy: String = "release_date", sortOrder: String = "desc"): Result<GetSetsResponse> {
        return try {
            val response: GetSetsResponse = client.get("${KtorApiClient.BASE_URL}/tcgApi/listar_sets") {
                parameter("tcg", tcgId)
                parameter("sortBy", sortBy)
                parameter("sortOrder", sortOrder)
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getCards(tcgId: String, setId: String, name: String, sortBy: String = "name", sortOrder: String = "desc",
                         page: Int = 1, limit: Int = 10): Result<GetCardsResponse> {
        return try {
            val response: GetCardsResponse = client.get("${KtorApiClient.BASE_URL}/tcgApi/buscar") {
                parameter("tcg_id", tcgId)
                parameter("set_id", setId)
                parameter("nombre", name)
                parameter("sortBy", sortBy)
                parameter("sortOrder", sortOrder)
                parameter("pagina", page)
                parameter("limite", limit)
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}