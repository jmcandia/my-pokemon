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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.random.Random

/**
 * Guarda el estado de la pantalla y contiene la lógica para pedir un Pokémon.
 * Sobrevive a los cambios de configuración (por ejemplo, rotar el dispositivo), así
 * que al girar la pantalla no se repite la petición.
 */
class PokemonViewModel(
    private val repository: PokemonRepository
) : ViewModel() {
    // Estado observable por Compose: cuando cambia, la UI se recompone sola.
    // `private set` impide que la UI lo modifique; solo el ViewModel lo cambia.
    var uiState: PokemonUiState by mutableStateOf(PokemonUiState.Loading)
        private set

    init {
        // Carga un primer Pokémon al crear el ViewModel.
        getRandomPokemon()
    }

    /** Pide un Pokémon con ID aleatorio y actualiza `uiState` con el resultado. */
    fun getRandomPokemon() {
        // `viewModelScope` cancela la corrutina si el ViewModel se destruye.
        viewModelScope.launch {
            uiState = PokemonUiState.Loading
            try {
                // PokéAPI tiene Pokémon del 1 al 1025 (`nextInt` excluye el límite superior).
                val randomId = Random.nextInt(1, 1026).toString()
                uiState = PokemonUiState.Success(repository.getPokemon(randomId))
            } catch (e: CancellationException) {
                // La corrutina se canceló (el ViewModel se destruye): hay que relanzar
                // la excepción para que la cancelación se propague.
                throw e
            } catch (e: IOException) {
                // Sin conexión, timeout o fallo de DNS.
                uiState = PokemonUiState.Error("Sin conexión. Revisa tu red e inténtalo de nuevo.")
            } catch (e: HttpException) {
                // El servidor respondió con error (404, 500…) o sin cuerpo.
                uiState = PokemonUiState.Error("El servidor respondió con error (${e.code()}).")
            } catch (e: Exception) {
                // Cualquier otro fallo, por ejemplo un JSON que Gson no puede leer.
                uiState = PokemonUiState.Error("Respuesta inesperada del servidor.")
            }
        }
    }

    companion object {
        /**
         * Crea el ViewModel con las dependencias reales (Retrofit). Se usa con
         * `viewModel(factory = PokemonViewModel.Factory)`. En los tests se construye
         * el ViewModel directamente, con un repositorio falso.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PokemonViewModel(PokemonRepository(RetrofitInstance.api))
            }
        }
    }
}
