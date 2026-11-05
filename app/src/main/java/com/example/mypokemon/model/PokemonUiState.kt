package com.example.mypokemon.model

sealed class PokemonUiState {
    object Loading : PokemonUiState()
    data class Success(val pokemon: Pokemon) : PokemonUiState()
    data class Error(val message: String) : PokemonUiState()
}