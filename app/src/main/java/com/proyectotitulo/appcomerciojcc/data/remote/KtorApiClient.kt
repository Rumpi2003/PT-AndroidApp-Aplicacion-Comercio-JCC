package com.proyectotitulo.appcomerciojcc.data.remote

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object KtorApiClient {

    var token: String? = null
    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        defaultRequest {
            url(BASE_URL)
            token?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
    }
    //const val BASE_URL = "http://10.0.2.2:3000/api"
    const val BASE_URL = "http://192.168.3.79:3000/api" // cambiar ip a la de tu pc
}