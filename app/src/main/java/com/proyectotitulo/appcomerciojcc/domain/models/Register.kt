package com.proyectotitulo.appcomerciojcc.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("correo")
    val email: String,
    @SerialName("contraseña")
    val password: String,
    @SerialName("nombre_usuario")
    val username: String,
    @SerialName("contacto")
    val contact: String,
    @SerialName("descripcion_perfil")
    val profileDescription: String,
    @SerialName("id_comuna")
    val communeId: Int
)

@Serializable
data class RegisterResponse(
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

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data class Success(val username: String, val message: String) : RegisterUiState
    data class Error(val errors: List<String>? = null) : RegisterUiState
}