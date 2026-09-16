package com.proyectotitulo.appcomerciojcc.data.repository

import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.domain.models.RegisterRequest
import com.proyectotitulo.appcomerciojcc.domain.models.RegisterResponse
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RegisterRepository {
    private val client = KtorApiClient.httpClient

    suspend fun doRegister(email: String, password: String, username: String, contact: String,
                           profileDescription: String, communeId: Int): Result<RegisterResponse> {
        return try {
            val response: RegisterResponse = client.post("${KtorApiClient.BASE_URL}/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, password, username, contact, profileDescription, communeId))
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}