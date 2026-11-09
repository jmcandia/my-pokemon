package cl.duoc.mypokemon.viewmodel

import cl.duoc.mypokemon.ui.utils.UiText

/**
 * Estados posibles de la pantalla. Al ser `sealed`, el `when` de la UI debe cubrir
 * los tres casos o no compila.
 */
sealed class PokemonUiState {
    // Petición en curso. `previous` es el Pokémon que se mostraba antes, si había
    // uno: la UI lo mantiene atenuado bajo el spinner para que la pantalla no salte.
    data class Loading(val previous: PokemonUi? = null) : PokemonUiState()
    // Petición correcta: contiene el Pokémon listo para mostrar.
    data class Success(val pokemon: PokemonUi) : PokemonUiState()
    // Petición fallida: contiene el mensaje que se muestra al usuario.
    data class Error(val message: UiText) : PokemonUiState()
}
