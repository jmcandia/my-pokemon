package cl.duoc.mypokemon.ui.components

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonTypePillContrastTest {

    // Los 18 tipos de PokéAPI más uno desconocido (cae en gris).
    private val types = listOf(
        "normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison",
        "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark",
        "steel", "fairy", "desconocido"
    )

    @Test
    fun `todos los tipos cumplen el contraste mínimo de WCAG AA`() {
        types.forEach { type ->
            val background = getPokemonTypeColor(type)
            val ratio = contrastRatio(readableTextColor(background), background)
            assertTrue("Contraste insuficiente en '$type': $ratio", ratio >= 4.5f)
        }
    }

    @Test
    fun `el texto blanco no se lee sobre Eléctrico, por eso se usa negro`() {
        val electric = getPokemonTypeColor("electric")

        assertTrue(contrastRatio(Color.White, electric) < 4.5f)
        assertEquals(Color.Black, readableTextColor(electric))
    }

    @Test
    fun `blanco y negro tienen el contraste máximo de 21 a 1`() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
    }
}
