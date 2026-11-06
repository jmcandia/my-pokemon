package com.example.mypokemon.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Punto único de configuración de Retrofit.
 *
 * Es un `object` (singleton): crear un cliente Retrofit es costoso, así que toda
 * la app comparte la misma instancia.
 */
object RetrofitInstance {
    // La URL base debe terminar en "/" para que Retrofit concatene bien las rutas.
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    // `by lazy` crea el cliente la primera vez que se usa `api`, no al cargar la clase.
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Gson convierte el JSON de la respuesta en las clases de `model/`.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            // Retrofit genera la implementación de la interfaz `ApiService`.
            .create(ApiService::class.java)
    }
}