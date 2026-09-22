package com.proyectotitulo.appcomerciojcc.domain.models

import android.provider.ContactsContract
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.time.Instant

@Serializable
data class PrivateProfileResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Profile,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Profile(
        @SerialName("comuna")
        val commune: Commune,
        @SerialName("correo")
        val email: String,
        @SerialName("nombre_usuario")
        val username: String,
        @SerialName("descripcion_perfil")
        val profileDescription: String,
        @SerialName("contacto")
        val contact: String,
        @SerialName("puntuacion_promedio")
        val averageScore: Float,
        @SerialName("radio_geo")
        val geoRadius: Int,
        @SerialName("visibilidad_perfil")
        val profileVisibility: Boolean,
        @SerialName("fecha_registro")
        val registerDate: String
    ) {
        val formattedRegisterDate: String
            get() = try {
                val zonedDateTime = ZonedDateTime.parse(registerDate)
                val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

                zonedDateTime.format(outputFormatter)
            } catch (e: Exception) {
                registerDate
            }
        @Serializable
        data class Commune(
            @SerialName("id_comuna")
            val communeId: Int,
            @SerialName("nombre_comuna")
            val communeName: String,
            @SerialName("region")
            val region: String,
        )
    }
}

@Serializable
data class CommuneResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: List<Commune>? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Commune(
        @SerialName("id_comuna")
        val communeId: Int,
        @SerialName("nombre_comuna")
        val communeName: String,
        @SerialName("region")
        val region: String,
    )
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profileData: PrivateProfileResponse.Profile) : ProfileUiState
    data class Error(val errors: List<String>? = null) : ProfileUiState
}