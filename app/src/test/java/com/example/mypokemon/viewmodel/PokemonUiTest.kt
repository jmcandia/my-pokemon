package com.example.mypokemon.viewmodel

import com.example.mypokemon.R
import com.example.mypokemon.model.Pokemon
import com.example.mypokemon.model.Type
import com.example.mypokemon.model.Types
import com.example.mypokemon.ui.utils.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class PokemonUiTest {

    private val spanish = Locale.forLanguageTag("es-CL")

    @Test
    fun `divide por 10 y usa coma decimal en español`() {
        assertEquals("6,0 kg", formatTenths(60, "kg", spanish))
        assertEquals("0,4 m", formatTenths(4, "m", spanish))
    }

    @Test
    fun `usa punto decimal en inglés`() {
        assertEquals("6.0 kg", formatTenths(60, "kg", Locale.US))
    }

    @Test
    fun `muestra un guion si falta el valor`() {
        assertEquals("—", formatTenths(null, "kg", spanish))
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

        val ui = pokemon.toUi(spanish)

        assertEquals(listOf("electric", "normal"), ui.types.map { it.key })
        assertEquals("6,0 kg", ui.weight)
        assertEquals("0,4 m", ui.height)
        assertNull(ui.imageUrl)
    }
}
