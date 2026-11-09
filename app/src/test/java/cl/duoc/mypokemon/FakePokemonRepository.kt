package cl.duoc.mypokemon

import cl.duoc.mypokemon.data.repository.PokemonRepository
import cl.duoc.mypokemon.model.Pokemon

/**
 * `PokemonRepository` falso para los tests del ViewModel: no usa la red ni Retrofit.
 * Cada test decide qué responde con `handler` (un Pokémon o lanzar una
 * `PokemonException`) y puede revisar los IDs pedidos en `requestedIds`.
 */
class FakePokemonRepository(
    var handler: suspend (idOrName: String) -> Pokemon
) : PokemonRepository {
    val requestedIds = mutableListOf<String>()

    override suspend fun getPokemon(idOrName: String): Pokemon {
        requestedIds += idOrName
        return handler(idOrName)
    }
}
