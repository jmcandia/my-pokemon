package cl.duoc.mypokemon.data.repository

import cl.duoc.mypokemon.model.Pokemon

/**
 * Fuente de datos de Pokémon para el ViewModel.
 *
 * Es una interfaz: el ViewModel depende de este contrato y no de una implementación
 * concreta. En la app se usa `DefaultPokemonRepository`; en los tests, un
 * repositorio falso.
 */
interface PokemonRepository {

    /**
     * Devuelve el Pokémon pedido.
     *
     * Si falla, lanza una `PokemonException` (`Network`, `Server` o `Unknown`).
     * Nunca lanza excepciones de Retrofit, OkHttp o Gson.
     */
    suspend fun getPokemon(idOrName: String): Pokemon
}
