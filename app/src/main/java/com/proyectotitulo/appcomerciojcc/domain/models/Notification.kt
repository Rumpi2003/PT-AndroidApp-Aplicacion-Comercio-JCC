package com.proyectotitulo.appcomerciojcc.domain.models

import com.proyectotitulo.appcomerciojcc.domain.models.GetMatchmakingResponse.Data
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceRequest(
    @SerialName("token")
    val token: String
)

@Serializable
data class RegisterDeviceResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Data? = null,
    @SerialName("errors")
    val errors: List<String>? = null
)

@Serializable
data class DeleteDeviceResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Data? = null,
    @SerialName("errors")
    val errors: List<String>? = null
)