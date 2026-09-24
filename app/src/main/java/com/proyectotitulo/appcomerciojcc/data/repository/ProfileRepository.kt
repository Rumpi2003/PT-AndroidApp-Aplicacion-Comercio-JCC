package com.proyectotitulo.appcomerciojcc.data.repository

import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.domain.models.GetCommuneResponse
import com.proyectotitulo.appcomerciojcc.domain.models.GetPrivateProfileResponse
import com.proyectotitulo.appcomerciojcc.domain.models.UpdateCommuneRequest
import com.proyectotitulo.appcomerciojcc.domain.models.UpdateCommuneResponse
import com.proyectotitulo.appcomerciojcc.domain.models.UpdateDescriptionRequest
import com.proyectotitulo.appcomerciojcc.domain.models.UpdateDescriptionResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ProfileRepository {
    private val client = KtorApiClient.httpClient

    suspend fun getCommunes(): Result<GetCommuneResponse> {
        return try {
            val response: GetCommuneResponse = client.get("${KtorApiClient.BASE_URL}/comunas/").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getPrivateProfile(): Result<GetPrivateProfileResponse> {
        return try {
            val response: GetPrivateProfileResponse = client.get("${KtorApiClient.BASE_URL}/usuarios/perfil_personal").body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateProfileDescription(profileDescription: String): Result<UpdateDescriptionResponse> {
        return try {
            val response: UpdateDescriptionResponse = client.put("${KtorApiClient.BASE_URL}/usuarios/descripcion") {
                contentType(ContentType.Application.Json)
                setBody(UpdateDescriptionRequest(profileDescription))
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateCommune(communeId: Int): Result<UpdateCommuneResponse> {
        return try {
            val response: UpdateCommuneResponse = client.put("${KtorApiClient.BASE_URL}/usuarios/comuna") {
                contentType(ContentType.Application.Json)
                setBody(UpdateCommuneRequest(communeId))
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}