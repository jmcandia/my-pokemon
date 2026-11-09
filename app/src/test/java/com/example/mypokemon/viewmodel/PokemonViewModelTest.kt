package com.example.mypokemon.viewmodel

import com.example.mypokemon.FakePokemonRepository
import com.example.mypokemon.MainDispatcherRule
import com.example.mypokemon.R
import com.example.mypokemon.data.repository.PokemonException
import com.example.mypokemon.model.Pokemon
import com.example.mypokemon.ui.utils.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class PokemonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pikachu = Pokemon(id = 25, name = "pikachu")

    @Test
    fun `empieza en Loading y pasa a Success con el Pokémon listo para mostrar`() = runTest {
        val viewModel = PokemonViewModel(FakePokemonRepository { pikachu })

        assertEquals(PokemonUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(PokemonUiState.Success(pikachu.toUi()), viewModel.uiState.value)
    }

    @Test
    fun `pide un ID entre 1 y 1025`() = runTest {
        val repository = FakePokemonRepository { pikachu }
        PokemonViewModel(repository)
        advanceUntilIdle()

        val id = repository.requestedIds.single().toInt()
        assertTrue("ID fuera de rango: $id", id in 1..1025)
    }

    @Test
    fun `muestra mensaje de conexión si falla la red`() = runTest {
        val viewModel = PokemonViewModel(
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
        val viewModel = PokemonViewModel(
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
        val viewModel = PokemonViewModel(
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
        val viewModel = PokemonViewModel(repository) // `init` lanza la primera petición.

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
        val viewModel = PokemonViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PokemonUiState.Error)

        repository.handler = { pikachu }
        viewModel.getRandomPokemon()
        advanceUntilIdle()

        assertEquals(PokemonUiState.Success(pikachu.toUi()), viewModel.uiState.value)
    }
}
