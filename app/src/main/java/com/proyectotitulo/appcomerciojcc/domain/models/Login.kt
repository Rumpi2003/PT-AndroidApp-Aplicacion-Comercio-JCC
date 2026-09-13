package com.proyectotitulo.appcomerciojcc.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("correo")
    val email: String,
    @SerialName("contraseña")
    val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: UserData? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class UserData(
        @SerialName("token")
        val token: String? = null,
        @SerialName("usuario")
        val user: User? = null
    ) {
        @Serializable
        data class User(
            @SerialName("id_usuario")
            val userId: Int?,
            @SerialName("correo")
            val email: String?,
            @SerialName("nombre_usuario")
            val username: String?
        )
    }
}

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Succes(val username: String, val message: String) : LoginUiState
    data class Error(val errors: List<String>? = null) : LoginUiState
}



