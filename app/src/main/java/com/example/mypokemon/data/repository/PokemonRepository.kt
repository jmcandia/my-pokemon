package com.example.mypokemon.data.repository

import com.example.mypokemon.data.remote.ApiService
import com.example.mypokemon.model.Pokemon
import retrofit2.HttpException

/**
 * Fuente de datos de Pokémon para el ViewModel.
 *
 * Recibe `ApiService` por constructor (inyección de dependencias). En la app se le
 * pasa `RetrofitInstance.api`; en un test se le puede pasar una API falsa.
 */
class PokemonRepository(
    private val api: ApiService
) {

    /**
     * Devuelve el Pokémon pedido.
     *
     * Si el servidor responde con error (404, 500…) o sin cuerpo, lanza
     * `HttpException`, que conserva el código HTTP. Los errores de red (sin
     * conexión, timeout) llegan como `IOException`. Ninguno se captura aquí: los
     * maneja el ViewModel.
     */
    suspend fun getPokemon(idOrName: String): Pokemon {
        val response = api.getPokemon(idOrName)
        return response.body()?.takeIf { response.isSuccessful }
            ?: throw HttpException(response)
    }
}
