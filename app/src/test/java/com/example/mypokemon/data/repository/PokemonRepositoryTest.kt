package com.example.mypokemon.data.repository

import com.example.mypokemon.FakeApiService
import com.example.mypokemon.model.Pokemon
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class PokemonRepositoryTest {

    private val pikachu = Pokemon(id = 25, name = "pikachu")

    // Ejecuta la petición y devuelve la excepción lanzada (o null si no falló).
    private suspend fun errorOf(repository: PokemonRepository): Throwable? =
        runCatching { repository.getPokemon("25") }.exceptionOrNull()

    @Test
    fun `devuelve el Pokémon si la respuesta es correcta`() = runTest {
        val repository = DefaultPokemonRepository(FakeApiService { Response.success(pikachu) })

        assertEquals(pikachu, repository.getPokemon("25"))
    }

    @Test
    fun `lanza NotFound si el servidor responde 404`() = runTest {
        val repository = DefaultPokemonRepository(
            FakeApiService { Response.error(404, "".toResponseBody()) }
        )

        val error = errorOf(repository)

        assertTrue(error is PokemonException.NotFound)
    }

    @Test
    fun `lanza Server con el código si el servidor responde 500`() = runTest {
        val repository = DefaultPokemonRepository(
            FakeApiService { Response.error(500, "".toResponseBody()) }
        )

        val error = errorOf(repository)

        assertTrue(error is PokemonException.Server)
        assertEquals(500, (error as PokemonException.Server).code)
    }

    @Test
    fun `lanza Server si la respuesta correcta llega sin cuerpo`() = runTest {
        val repository = DefaultPokemonRepository(FakeApiService { Response.success(null) })

        val error = errorOf(repository)

        assertTrue(error is PokemonException.Server)
        assertEquals(200, (error as PokemonException.Server).code)
    }

    @Test
    fun `traduce los errores de red a Network`() = runTest {
        val repository = DefaultPokemonRepository(FakeApiService { throw IOException("sin red") })

        val error = errorOf(repository)

        assertTrue(error is PokemonException.Network)
        assertTrue(error?.cause is IOException)
    }

    @Test
    fun `traduce un JSON ilegible a Unknown`() = runTest {
        val repository = DefaultPokemonRepository(
            FakeApiService { throw JsonSyntaxException("JSON inválido") }
        )

        val error = errorOf(repository)

        assertTrue(error is PokemonException.Unknown)
    }
}
