package com.example.mypokemon.viewmodel

import com.example.mypokemon.R
import com.example.mypokemon.model.Pokemon
import com.example.mypokemon.model.Type
import com.example.mypokemon.model.Types
import com.example.mypokemon.ui.utils.UiText
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PokemonUiTest {

    @Test
    fun `divide por 10 y deja el formato al recurso`() {
        // El separador decimal lo pone Android al resolver el recurso, según el idioma.
        assertEquals(
            UiText.Resource(R.string.weight_value, listOf(6.0)),
            formatTenths(60, R.string.weight_value)
        )
        assertEquals(
            UiText.Resource(R.string.height_value, listOf(0.4)),
            formatTenths(4, R.string.height_value)
        )
    }

    @Test
    fun `muestra un guion si falta el valor`() {
        assertEquals(UiText.Plain("—"), formatTenths(null, R.string.weight_value))
    }

    @Test
    fun `título con número y nombre capitalizado`() {
        assertEquals(
            UiText.Resource(R.string.pokemon_title, listOf(25, "Pikachu")),
            formatTitle(25, "pikachu")
        )
    }

    @Test
    fun `título sin número muestra solo el nombre`() {
        assertEquals(UiText.Plain("Pikachu"), formatTitle(null, "pikachu"))
    }

    @Test
    fun `título sin nombre muestra Pokémon desconocido`() {
        assertEquals(
            UiText.Resource(R.string.pokemon_title_unknown, listOf(25)),
            formatTitle(25, null)
        )
        assertEquals(UiText.Resource(R.string.pokemon_unknown), formatTitle(null, null))
    }

    @Test
    fun `traduce los tipos conocidos con su recurso`() {
        assertEquals(
            PokemonTypeUi(key = "electric", label = UiText.Resource(R.string.type_electric)),
            typeUi("Electric")
        )
    }

    @Test
    fun `un tipo desconocido queda capitalizado`() {
        assertEquals(
            PokemonTypeUi(key = "stellar", label = UiText.Plain("Stellar")),
            typeUi("stellar")
        )
    }

    @Test
    fun `convierte el DTO completo y un tipo sin nombre cae en normal`() {
        val pokemon = Pokemon(
            id = 25,
            name = "pikachu",
            height = 4,
            weight = 60,
            types = arrayListOf(
                Types(slot = 1, type = Type(name = "electric")),
                Types(slot = 2, type = null)
            )
        )

        val ui = pokemon.toUi()

        assertEquals(listOf("electric", "normal"), ui.types.map { it.key })
        assertEquals(UiText.Resource(R.string.weight_value, listOf(6.0)), ui.weight)
        assertEquals(UiText.Resource(R.string.height_value, listOf(0.4)), ui.height)
        assertNull(ui.imageUrl)
    }

    @Test
    fun `no falla si Gson recibe nulls explícitos en el JSON`() {
        // Gson ignora la nulabilidad de Kotlin: asigna `null` aunque haya default.
        val json = """{"id": 25, "name": null, "sprites": null, "types": null}"""
        val pokemon = Gson().fromJson(json, Pokemon::class.java)

        val ui = pokemon.toUi()

        assertEquals(emptyList<PokemonTypeUi>(), ui.types)
        assertEquals(UiText.Resource(R.string.pokemon_title_unknown, listOf(25)), ui.title)
        assertNull(ui.imageUrl)
        assertEquals(UiText.Plain("—"), ui.weight)
    }
}
