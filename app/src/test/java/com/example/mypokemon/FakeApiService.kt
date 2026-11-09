package com.example.mypokemon

import com.example.mypokemon.data.remote.ApiService
import com.example.mypokemon.model.Pokemon
import retrofit2.Response

/**
 * `ApiService` falso para tests: no usa la red. Cada test decide qué responde con
 * `handler` (una respuesta, un error HTTP o lanzar una excepción) y puede revisar
 * los IDs pedidos en `requestedIds`.
 */
class FakeApiService(
    var handler: suspend (idOrName: String) -> Response<Pokemon>
) : ApiService {
    val requestedIds = mutableListOf<String>()

    override suspend fun getPokemon(idOrName: String): Response<Pokemon> {
        requestedIds += idOrName
        return handler(idOrName)
    }
}
