package com.proyectotitulo.appcomerciojcc.domain.models

import com.proyectotitulo.appcomerciojcc.domain.models.UpdateDescriptionResponse.ProfileDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class GetPrivateProfileResponse(
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
data class GetCommuneResponse(
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

@Serializable
data class UpdateDescriptionRequest(
    @SerialName("descripcion_perfil")
    val profileDescription: String
)

@Serializable
data class UpdateDescriptionResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: ProfileDescription,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class ProfileDescription(
        @SerialName("descripcion_perfil")
        val profileDescription: String
    )
}

@Serializable
data class UpdateCommuneRequest(
    @SerialName("id_comuna")
    val communeId: Int
)

@Serializable
data class UpdateCommuneResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Commune,
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
    data class Success(val profileData: GetPrivateProfileResponse.Profile) : ProfileUiState
    data class Error(val errors: List<String>? = null) : ProfileUiState
}