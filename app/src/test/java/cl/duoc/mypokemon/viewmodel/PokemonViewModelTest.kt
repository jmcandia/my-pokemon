package cl.duoc.mypokemon.viewmodel

import androidx.lifecycle.SavedStateHandle
import cl.duoc.mypokemon.FakePokemonRepository
import cl.duoc.mypokemon.MainDispatcherRule
import cl.duoc.mypokemon.R
import cl.duoc.mypokemon.data.repository.PokemonException
import cl.duoc.mypokemon.model.Pokemon
import cl.duoc.mypokemon.ui.utils.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.random.Random

class PokemonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pikachu = Pokemon(id = 25, name = "pikachu")
    private val bulbasaur = Pokemon(id = 1, name = "bulbasaur")

    private fun createViewModel(
        repository: FakePokemonRepository,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        random: Random = Random.Default
    ) = PokemonViewModel(repository, savedStateHandle, random)

    @Test
    fun `empieza en Loading y pasa a Success con el Pokémon listo para mostrar`() = runTest {
        val viewModel = createViewModel(FakePokemonRepository { pikachu })

        assertEquals(PokemonUiState.Loading(), viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(PokemonUiState.Success(pikachu.toUi()), viewModel.uiState.value)
    }

    @Test
    fun `pide un ID entre 1 y 1025`() = runTest {
        val repository = FakePokemonRepository { pikachu }
        createViewModel(repository)
        advanceUntilIdle()

        val id = repository.requestedIds.single().toInt()
        assertTrue("ID fuera de rango: $id", id in 1..1025)
    }

    @Test
    fun `pide el ID que da el Random inyectado`() = runTest {
        // Dos `Random` con la misma semilla dan la misma secuencia de números.
        val expectedIds = Random(SEED).let { r -> List(2) { r.nextInt(1, 1026).toString() } }
        val repository = FakePokemonRepository { pikachu }
        val viewModel = createViewModel(repository, random = Random(SEED))
        advanceUntilIdle()

        viewModel.getRandomPokemon()
        advanceUntilIdle()

        assertEquals(expectedIds, repository.requestedIds)
    }

    @Test
    fun `muestra el Pokémon anterior mientras carga el siguiente`() = runTest {
        val repository = FakePokemonRepository { pikachu }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.handler = {
            delay(1_000) // Tiempo virtual: simula una respuesta lenta.
            bulbasaur
        }
        viewModel.getRandomPokemon()
        runCurrent() // Arranca la petición, pero no deja pasar el `delay`.

        assertEquals(PokemonUiState.Loading(previous = pikachu.toUi()), viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(PokemonUiState.Success(bulbasaur.toUi()), viewModel.uiState.value)
    }

    @Test
    fun `tras un error la carga siguiente no tiene Pokémon anterior`() = runTest {
        val repository = FakePokemonRepository {
            throw PokemonException.Network(IOException("sin red"))
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.handler = {
            delay(1_000)
            pikachu
        }
        viewModel.getRandomPokemon()
        runCurrent()

        assertEquals(PokemonUiState.Loading(previous = null), viewModel.uiState.value)
    }

    @Test
    fun `recupera el último Pokémon si Android cerró la app`() = runTest {
        // El `SavedStateHandle` es lo que Android conserva al cerrar el proceso.
        val savedStateHandle = SavedStateHandle()
        createViewModel(FakePokemonRepository { pikachu }, savedStateHandle)
        advanceUntilIdle()

        // Un ViewModel nuevo con el mismo estado guardado pide el Pokémon mostrado
        // (Pikachu, el 25), no uno aleatorio.
        val secondRepository = FakePokemonRepository { pikachu }
        createViewModel(secondRepository, savedStateHandle)
        advanceUntilIdle()

        assertEquals(listOf("25"), secondRepository.requestedIds)
    }

    @Test
    fun `busca por nombre normalizado y guarda el número del resultado`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val repository = FakePokemonRepository { pikachu }
        val viewModel = createViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        viewModel.searchPokemon("  Pikachu ")
        advanceUntilIdle()

        assertEquals("pikachu", repository.requestedIds.last())
        assertEquals(PokemonUiState.Success(pikachu.toUi()), viewModel.uiState.value)
        // Al recrearse tras cerrar la app, pide por número aunque se buscó por nombre.
        val restored = FakePokemonRepository { pikachu }
        createViewModel(restored, savedStateHandle)
        advanceUntilIdle()
        assertEquals(listOf("25"), restored.requestedIds)
    }

    @Test
    fun `ignora una búsqueda vacía`() = runTest {
        val repository = FakePokemonRepository { pikachu }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.searchPokemon("   ")
        advanceUntilIdle()

        assertEquals(1, repository.requestedIds.size) // Solo la carga inicial.
    }

    @Test
    fun `muestra qué se buscó si el Pokémon no existe`() = runTest {
        val repository = FakePokemonRepository { pikachu }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.handler = { throw PokemonException.NotFound() }
        viewModel.searchPokemon("Agumon")
        advanceUntilIdle()

        assertEquals(
            PokemonUiState.Error(UiText.Resource(R.string.error_not_found, listOf("agumon"))),
            viewModel.uiState.value
        )
    }

    @Test
    fun `no guarda el ID de una petición fallida`() = runTest {
        val savedStateHandle = SavedStateHandle()
        createViewModel(
            FakePokemonRepository { throw PokemonException.Server(500) },
            savedStateHandle
        )
        advanceUntilIdle()

        assertTrue(savedStateHandle.keys().isEmpty())
    }

    @Test
    fun `muestra mensaje de conexión si falla la red`() = runTest {
        val viewModel = createViewModel(
            FakePokemonRepository { throw PokemonException.Network(IOException("timeout")) }
        )
        advanceUntilIdle()

        assertEquals(
            PokemonUiState.Error(UiText.Resource(R.string.error_network)),
            viewModel.uiState.value
        )
    }

    @Test
    fun `muestra el código si el servidor responde con error`() = runTest {
        val viewModel = createViewModel(
            FakePokemonRepository { throw PokemonException.Server(500) }
        )
        advanceUntilIdle()

        assertEquals(
            PokemonUiState.Error(UiText.Resource(R.string.error_server, listOf(500))),
            viewModel.uiState.value
        )
    }

    @Test
    fun `muestra mensaje genérico si la respuesta no se puede leer`() = runTest {
        val viewModel = createViewModel(
            FakePokemonRepository { throw PokemonException.Unknown(IllegalStateException()) }
        )
        advanceUntilIdle()

        assertEquals(
            PokemonUiState.Error(UiText.Resource(R.string.error_unknown)),
            viewModel.uiState.value
        )
    }

    @Test
    fun `ignora nuevas peticiones mientras otra está en curso`() = runTest {
        val repository = FakePokemonRepository {
            delay(1_000) // Tiempo virtual: simula una respuesta lenta.
            pikachu
        }
        val viewModel = createViewModel(repository) // `init` lanza la primera petición.

        viewModel.getRandomPokemon()
        viewModel.getRandomPokemon()
        advanceUntilIdle()
        assertEquals(1, repository.requestedIds.size)

        // Una vez terminada, se puede pedir otra.
        viewModel.getRandomPokemon()
        advanceUntilIdle()
        assertEquals(2, repository.requestedIds.size)
    }

    @Test
    fun `permite reintentar después de un error`() = runTest {
        val repository = FakePokemonRepository {
            throw PokemonException.Network(IOException("sin red"))
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PokemonUiState.Error)

        repository.handler = { pikachu }
        viewModel.getRandomPokemon()
        advanceUntilIdle()

        assertEquals(PokemonUiState.Success(pikachu.toUi()), viewModel.uiState.value)
    }

    private companion object {
        const val SEED = 42
    }
}
