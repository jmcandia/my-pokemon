package com.example.mypokemon.data

import com.example.mypokemon.data.remote.RetrofitInstance
import com.example.mypokemon.data.repository.DefaultPokemonRepository
import com.example.mypokemon.data.repository.PokemonRepository

/**
 * Contenedor de dependencias de la app (inyección manual, sin Hilt ni Koin).
 *
 * Es el único sitio que decide qué implementación se usa para cada dependencia. Lo
 * crea `PokemonApplication` y vive mientras viva la app.
 */
interface AppContainer {
    val pokemonRepository: PokemonRepository
}

/** Contenedor real: el repositorio pide los datos a PokéAPI con Retrofit. */
class DefaultAppContainer : AppContainer {
    // `by lazy`: el repositorio se crea la primera vez que alguien lo pide.
    override val pokemonRepository: PokemonRepository by lazy {
        DefaultPokemonRepository(RetrofitInstance.api)
    }
}
