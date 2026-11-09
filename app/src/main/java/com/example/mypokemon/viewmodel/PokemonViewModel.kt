package com.example.mypokemon.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mypokemon.PokemonApplication
import com.example.mypokemon.R
import com.example.mypokemon.data.repository.PokemonException
import com.example.mypokemon.data.repository.PokemonRepository
import com.example.mypokemon.ui.utils.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Guarda el estado de la pantalla y contiene la lógica para pedir un Pokémon.
 * Sobrevive a los cambios de configuración (por ejemplo, rotar el dispositivo), así
 * que al girar la pantalla no se repite la petición.
 *
 * `savedStateHandle` guarda el ID del último Pokémon mostrado. Si Android cierra la
 * app en segundo plano para liberar memoria, al volver se pide el mismo Pokémon en
 * lugar de uno nuevo.
 *
 * `random` elige los IDs aleatorios. La app usa `Random.Default`; los tests pasan un
 * `Random` con semilla para saber qué ID se pedirá.
 */
class PokemonViewModel(
    private val repository: PokemonRepository,
    private val savedStateHandle: SavedStateHandle,
    private val random: Random = Random.Default
) : ViewModel() {
    // Estado mutable, privado: solo el ViewModel lo cambia.
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Loading())

    // Estado de solo lectura que observa la UI con `collectAsStateWithLifecycle()`.
    // Al ser un `StateFlow`, el ViewModel no depende de Compose y se testea leyendo `.value`.
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()

    // Petición en curso, para no lanzar otra mientras no termine.
    private var loadJob: Job? = null

    init {
        // Al crear el ViewModel carga el último Pokémon mostrado, si se guardó antes
        // de que Android cerrara la app, o uno aleatorio.
        loadPokemon(savedStateHandle[KEY_LAST_ID] ?: randomId())
    }

    /** Pide un Pokémon con ID aleatorio y actualiza `uiState` con el resultado. */
    fun getRandomPokemon() {
        loadPokemon(randomId())
    }

    // PokéAPI tiene Pokémon del 1 al 1025 (`nextInt` excluye el límite superior).
    private fun randomId(): Int = random.nextInt(1, 1026)

    /**
     * Pide el Pokémon `id` y actualiza `uiState` con el resultado.
     *
     * Si ya hay una petición en curso, no hace nada. La UI también desactiva el botón
     * mientras carga; esta comprobación evita peticiones simultáneas aunque se llame
     * desde otro sitio.
     */
    private fun loadPokemon(id: Int) {
        if (loadJob?.isActive == true) return

        // El Pokémon que se ve ahora, para seguir mostrándolo mientras carga el nuevo.
        val previous = (_uiState.value as? PokemonUiState.Success)?.pokemon

        // `viewModelScope` cancela la corrutina si el ViewModel se destruye.
        loadJob = viewModelScope.launch {
            _uiState.value = PokemonUiState.Loading(previous)
            _uiState.value = try {
                val pokemon = repository.getPokemon(id.toString())
                // Solo se guarda un ID que cargó bien: es el que se está mostrando.
                savedStateHandle[KEY_LAST_ID] = id
                // Convierte el DTO de red en un modelo listo para mostrar.
                PokemonUiState.Success(pokemon.toUi())
            } catch (e: PokemonException) {
                // El repositorio solo lanza `PokemonException`. La cancelación de la
                // corrutina (`CancellationException`) no entra aquí y se propaga sola.
                PokemonUiState.Error(e.toMessage())
            }
        }
    }

    // Elige el mensaje de cada tipo de error. El texto está en `strings.xml`.
    private fun PokemonException.toMessage(): UiText = when (this) {
        is PokemonException.Network -> UiText.Resource(R.string.error_network)
        is PokemonException.Server -> UiText.Resource(R.string.error_server, listOf(code))
        is PokemonException.Unknown -> UiText.Resource(R.string.error_unknown)
    }

    companion object {
        // Clave del ID guardado en `savedStateHandle`.
        private const val KEY_LAST_ID = "last_pokemon_id"

        /**
         * Crea el ViewModel con el repositorio del `AppContainer` de la app y el
         * `SavedStateHandle` que da Android. Se usa con
         * `viewModel(factory = PokemonViewModel.Factory)`. En los tests se construye
         * el ViewModel directamente, con un repositorio falso.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as PokemonApplication
                PokemonViewModel(
                    repository = application.container.pokemonRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
