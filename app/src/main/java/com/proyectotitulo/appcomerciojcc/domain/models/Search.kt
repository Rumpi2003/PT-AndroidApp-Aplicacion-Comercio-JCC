package com.proyectotitulo.appcomerciojcc.domain.models

import com.proyectotitulo.appcomerciojcc.domain.models.GetPrivateProfileResponse.Profile
import com.proyectotitulo.appcomerciojcc.domain.models.GetTcgsResponse.TCG
import com.proyectotitulo.appcomerciojcc.domain.models.GetSetsResponse.Data.Set

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetTcgsResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: List<TCG>? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class TCG(
        @SerialName("id")
        val tcgId: String?,
        @SerialName("nombre_tcg")
        val tcgName: String?
    )
}

@Serializable
data class GetSetsResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: Data? = null,
    @SerialName("errors")
    val errors: List<String>? = null
) {
    @Serializable
    data class Data(
        @SerialName("resultados")
        val results: List<Set>?,
        @SerialName("total")
        val total: Int
    ) {
        @Serializable
        data class Set(
            @SerialName("id")
            val setId: String?,
            @SerialName("nombre_set")
            val setName: String?
        )
    }
}

data class SearchUiState(
    val query: String = "",
    val selectedTcg: TCG? = null,
    val selectedSet: Set? = null,

    val tcgsList: List<TCG> = emptyList(),
    val setsList: List<Set> = emptyList(),

    val setSortBy: String = "release_date",
    val setSortOrder: String = "desc",

    val cardsList: List<String> = emptyList(),

    val isLoadingTcgs: Boolean = false,
    val isLoadingSets: Boolean = false,
    val isSearchingCards: Boolean = false,

    val errors: List<String>? = null
)