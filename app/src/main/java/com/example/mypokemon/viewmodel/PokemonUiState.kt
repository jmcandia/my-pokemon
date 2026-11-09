package com.example.mypokemon.viewmodel

import com.example.mypokemon.ui.utils.UiText

/**
 * Estados posibles de la pantalla. Al ser `sealed`, el `when` de la UI debe cubrir
 * los tres casos o no compila.
 */
sealed class PokemonUiState {
    // Petición en curso.
    object Loading : PokemonUiState()
    // Petición correcta: contiene el Pokémon listo para mostrar.
    data class Success(val pokemon: PokemonUi) : PokemonUiState()
    // Petición fallida: contiene el mensaje que se muestra al usuario.
    data class Error(val message: UiText) : PokemonUiState()
}
