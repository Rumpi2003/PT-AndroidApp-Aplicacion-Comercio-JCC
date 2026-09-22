package com.proyectotitulo.appcomerciojcc.data.repository

import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.domain.models.CommuneResponse
import com.proyectotitulo.appcomerciojcc.domain.models.LoginResponse
import com.proyectotitulo.appcomerciojcc.domain.models.PrivateProfileResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ProfileRepository {
    private val client = KtorApiClient.httpClient

    suspend fun getCommunes(): Result<CommuneResponse> {
        return try {
            val response: CommuneResponse = client.get("${KtorApiClient.BASE_URL}/comunas/").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getPrivateProfile(): Result<PrivateProfileResponse> {
        return try {
            val response: PrivateProfileResponse = client.get("${KtorApiClient.BASE_URL}/usuarios/perfil_personal").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}