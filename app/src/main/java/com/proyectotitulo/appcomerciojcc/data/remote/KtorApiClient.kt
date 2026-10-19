package com.proyectotitulo.appcomerciojcc.data.remote

import android.content.Context
import coil.Coil
import coil.ImageLoader
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

object KtorApiClient {

    var token: String? = null
    var onUnauthorized: (() -> Unit)? = null

    fun setupCoil(context: Context) {
        val coilOkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.64 Mobile Safari/537.36")
                    .header("Referer", "https://www.tcgplayer.com/")
                    .header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                    .build()
                chain.proceed(request)
            }
            .build()

        val imageLoader = ImageLoader.Builder(context)
            .okHttpClient(coilOkHttpClient)
            .crossfade(true)
            .build()

        Coil.setImageLoader(imageLoader)
    }
    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        HttpResponseValidator {
            validateResponse { response ->
                if (response.status == HttpStatusCode.Unauthorized) {
                    val requestPath = response.call.request.url.encodedPath
                    if (!requestPath.contains("/auth/login") && !requestPath.contains("/auth/register")) {
                        onUnauthorized?.invoke()
                    }
                }
            }
        }

        defaultRequest {
            url(BASE_URL)
            token?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
    }
    // const val BASE_URL = "http://10.0.2.2:3000/api"
    const val BASE_URL = "http://192.168.1.100:3000/api" // cambiar ip a la de tu pc
}