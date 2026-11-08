package com.proyectotitulo.appcomerciojcc.data.repository

import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.domain.models.DeviceRequest
import com.proyectotitulo.appcomerciojcc.domain.models.GetPublicProfileResponse
import com.proyectotitulo.appcomerciojcc.domain.models.RegisterDeviceResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class NotificationRepository {
    private val client = KtorApiClient.httpClient

    suspend fun registerDevice(token: String): Result<RegisterDeviceResponse> {
        return try {
            val response: RegisterDeviceResponse = client.post("${KtorApiClient.BASE_URL}/dispositivos/") {
                contentType(ContentType.Application.Json)
                setBody(
                    DeviceRequest(
                        token = token
                    )
                )
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteDevice(token: String): Result<RegisterDeviceResponse> {
        return try {
            val response: RegisterDeviceResponse = client.delete("${KtorApiClient.BASE_URL}/dispositivos/") {
                contentType(ContentType.Application.Json)
                setBody(
                    DeviceRequest(
                        token = token
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