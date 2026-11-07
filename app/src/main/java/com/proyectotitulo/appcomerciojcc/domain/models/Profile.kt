package com.proyectotitulo.appcomerciojcc.domain.models

import com.proyectotitulo.appcomerciojcc.domain.models.GetPublicProfileResponse.Profile
import com.proyectotitulo.appcomerciojcc.domain.models.UpdateVisibilityResponse.ProfileVisibility
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
    val data: Profile? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Profile(
        @SerialName("id_usuario")
        val userId: Int? = null,
        @SerialName("comuna")
        val commune: Commune?,
        @SerialName("correo")
        val email: String?,
        @SerialName("nombre_usuario")
        val username: String?,
        @SerialName("descripcion_perfil")
        val profileDescription: String?,
        @SerialName("contacto")
        val contact: String?,
        @SerialName("puntuacion_promedio")
        val averageScore: Float?,
        @SerialName("radio_geo")
        val geoRadius: Int?,
        @SerialName("visibilidad_perfil")
        val profileVisibility: Boolean?,
        @SerialName("fecha_registro")
        val registerDate: String?
    ) {
        val formattedRegisterDate: String?
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
            val communeId: Int?,
            @SerialName("nombre_comuna")
            val communeName: String?,
            @SerialName("region")
            val region: String?,
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
        val communeId: Int?,
        @SerialName("nombre_comuna")
        val communeName: String?,
        @SerialName("region")
        val region: String?,
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
    val data: ProfileDescription? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class ProfileDescription(
        @SerialName("descripcion_perfil")
        val profileDescription: String?
    )
}

@Serializable
data class UpdateCommuneRequest(
    @SerialName("id_comuna")
    val communeId: Int?
)

@Serializable
data class UpdateCommuneResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Commune? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Commune(
        @SerialName("id_comuna")
        val communeId: Int?,
        @SerialName("nombre_comuna")
        val communeName: String?,
        @SerialName("region")
        val region: String?,
    )
}

@Serializable
data class UpdateGeoRadiusRequest(
    @SerialName("radio_geo")
    val geoRadius: Int
)

@Serializable
data class UpdateGeoRadiusResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: GeoRadius? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class GeoRadius(
        @SerialName("radio_geo")
        val geoRadius: Int? = null
    )
}

@Serializable
data class UpdateVisibilityRequest(
    @SerialName("visibilidad_perfil")
    val profileVisibility: Boolean
)

@Serializable
data class UpdateVisibilityResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: ProfileVisibility?  = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class ProfileVisibility(
        @SerialName("visibilidad_perfil")
        val profileVisibility: Boolean? = null
    )
}

@Serializable
data class GetPublicProfileResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Profile?  = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Profile(
        @SerialName("id_usuario")
        val userId: Int? = null,
        @SerialName("nombre_usuario")
        val username: String? = null,
        @SerialName("descripcion_perfil")
        val profileDescription: String? = null,
        @SerialName("puntuacion_promedio")
        val averageScore: Float? = null,
        @SerialName("total_resenas")
        val totalReviews: Int? = null,
        @SerialName("fecha_registro")
        val registerDate: String? = null,
        @SerialName("comuna")
        val commune: Commune? = null,
        @SerialName("contacto")
        val contact: String? = null
    ) {
        val formattedRegisterDate: String?
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
            val communeId: Int?,
            @SerialName("nombre_comuna")
            val communeName: String?,
            @SerialName("region")
            val region: String?,
        )
    }
}

@Serializable
data class GetReviewsResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Data?  = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Data(
        @SerialName("total")
        val totalReviews: Int? = null,
        @SerialName("resultados")
        val results: List<Review>? = null
    ) {
        @Serializable
        data class Review(
            @SerialName("autor")
            val author: User? = null,
            @SerialName("puntuacion")
            val score: Int? = null,
            @SerialName("cuerpo")
            val content: String? = null,
            @SerialName("fecha_creacion")
            val creationDate: String? = null
        ) {
            val formattedCreationDate: String?
                get() = try {
                    val zonedDateTime = ZonedDateTime.parse(creationDate)
                    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

                    zonedDateTime.format(outputFormatter)
                } catch (e: Exception) {
                    creationDate
                }
            @Serializable
            data class User(
                @SerialName("id_usuario")
                val userId: Int? = null,
                @SerialName("nombre_usuario")
                val username: String? = null
            )
        }
    }
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profileData: GetPrivateProfileResponse.Profile?) : ProfileUiState
    data class Error(val errors: List<String>? = null) : ProfileUiState
}
