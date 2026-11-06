package com.example.mypokemon.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mypokemon.data.remote.RetrofitInstance
import com.example.mypokemon.data.repository.PokemonRepository
import com.example.mypokemon.model.PokemonUiState
import kotlinx.coroutines.launch
import kotlin.random.Random

class PokemonViewModel(
    private val repository: PokemonRepository
) : ViewModel() {
    var uiState: PokemonUiState by mutableStateOf(PokemonUiState.Loading)
        private set

    init {
        getRandomPokemon()
    }

    fun getRandomPokemon() {
        viewModelScope.launch {
            uiState = PokemonUiState.Loading
            try {
                val randomId = Random.nextInt(1, 1026).toString()
                val pokemon = repository.getPokemon(randomId)

                uiState = if (pokemon != null) {
                    PokemonUiState.Success(pokemon)
                } else {
                    PokemonUiState.Error("Error en la respuesta del servidor")
                }
            } catch (e: Exception) {
                uiState = PokemonUiState.Error("Error de red: ${e.localizedMessage}")
            }
        }
    }

    companion object {
        /** Wires the production dependencies. Tests build the ViewModel directly with a fake repository. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PokemonViewModel(PokemonRepository(RetrofitInstance.api))
            }
        }
    }
}
