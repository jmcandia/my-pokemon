package com.example.mypokemon.data.remote

import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * Punto único de configuración de Retrofit.
 *
 * Crear un cliente Retrofit es costoso, así que la app debe usar una sola instancia.
 * `AppContainer` llama a `createApi` una vez y guarda el resultado. Necesita la
 * carpeta de caché de la app, que solo se conoce con un `Context`.
 */
object RetrofitInstance {
    // La URL base debe terminar en "/" para que Retrofit concatene bien las rutas.
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    // Tamaño máximo de la caché HTTP en disco. Cada respuesta de PokéAPI pesa unos
    // 300 KB, así que caben unos treinta Pokémon.
    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024

    /** Crea el `ApiService` con caché HTTP en `cacheDir/http`. */
    fun createApi(cacheDir: File): ApiService {
        // OkHttp es el cliente HTTP que usa Retrofit por debajo. Se configura aparte
        // para añadirle la caché y el interceptor que la usa sin conexión.
        val client = OkHttpClient.Builder()
            .cache(Cache(File(cacheDir, "http"), CACHE_SIZE_BYTES))
            .addInterceptor(OfflineCacheInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            // Gson convierte el JSON de la respuesta en las clases de `model/`.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            // Retrofit genera la implementación de la interfaz `ApiService`.
            .create(ApiService::class.java)
    }
}
