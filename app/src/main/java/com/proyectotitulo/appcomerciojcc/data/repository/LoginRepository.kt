package com.proyectotitulo.appcomerciojcc.data.repository

import com.proyectotitulo.appcomerciojcc.domain.models.LoginRequest
import com.proyectotitulo.appcomerciojcc.domain.models.LoginResponse
import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class LoginRepository {
    private val client = KtorApiClient.httpClient

    suspend fun doLogin(email: String, password: String): Result<LoginResponse> {
        return try {
            val response: LoginResponse = client.post("${KtorApiClient.BASE_URL}/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}