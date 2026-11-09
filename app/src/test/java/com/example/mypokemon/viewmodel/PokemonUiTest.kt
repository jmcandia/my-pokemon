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
    fun `los nombres con guiones se muestran con espacios y mayúsculas`() {
        assertEquals("Mr Mime", formatName("mr-mime"))
        assertEquals("Toxtricity Amped", formatName("toxtricity-amped"))
        assertEquals(
            UiText.Resource(R.string.pokemon_title, listOf(122, "Mr Mime")),
            formatTitle(122, "mr-mime")
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
    fun `traduce las estadísticas y calcula la barra respecto a 255`() {
        assertEquals(
            PokemonStatUi(UiText.Resource(R.string.stat_attack), 51, 0.2f),
            statUi("attack", 51)
        )
        // Una estadística desconocida se muestra con su nombre formateado.
        assertEquals(UiText.Plain("Accuracy"), statUi("accuracy", 10).label)
        // La barra nunca pasa del 100 %.
        assertEquals(1f, statUi("hp", 300).fraction)
    }

    @Test
    fun `formatea las habilidades y marca las ocultas`() {
        assertEquals(UiText.Plain("Static"), abilityUi("static", isHidden = false))
        assertEquals(
            UiText.Resource(R.string.ability_hidden, listOf("Lightning rod")),
            abilityUi("lightning-rod", isHidden = true)
        )
    }

    @Test
    fun `lee estadísticas, habilidades y grito con los nombres reales del JSON`() {
        // Fragmento real de la respuesta de PokéAPI (claves en snake_case).
        val json = """
            {
              "id": 25,
              "stats": [
                {"base_stat": 35, "effort": 0, "stat": {"name": "hp", "url": "x"}},
                {"base_stat": null, "stat": {"name": "speed"}}
              ],
              "abilities": [
                {"is_hidden": false, "slot": 1, "ability": {"name": "static", "url": "x"}},
                {"is_hidden": true, "slot": 3, "ability": {"name": "lightning-rod", "url": "x"}},
                {"is_hidden": false, "ability": null}
              ],
              "cries": {"latest": "https://example.com/25.ogg", "legacy": "x"}
            }
        """.trimIndent()

        val ui = Gson().fromJson(json, Pokemon::class.java).toUi()

        // La estadística sin valor y la habilidad sin nombre se omiten.
        assertEquals(listOf(statUi("hp", 35)), ui.stats)
        assertEquals(
            listOf(abilityUi("static", false), abilityUi("lightning-rod", true)),
            ui.abilities
        )
        assertEquals("https://example.com/25.ogg", ui.cryUrl)
    }

    @Test
    fun `no falla si Gson recibe nulls explícitos en el JSON`() {
        // Gson ignora la nulabilidad de Kotlin: asigna `null` aunque haya default.
        val json = """
            {"id": 25, "name": null, "sprites": null, "types": null,
             "stats": null, "abilities": null, "cries": null}
        """.trimIndent()
        val pokemon = Gson().fromJson(json, Pokemon::class.java)

        val ui = pokemon.toUi()

        assertEquals(emptyList<PokemonTypeUi>(), ui.types)
        assertEquals(UiText.Resource(R.string.pokemon_title_unknown, listOf(25)), ui.title)
        assertNull(ui.imageUrl)
        assertEquals(UiText.Plain("—"), ui.weight)
        assertEquals(emptyList<PokemonStatUi>(), ui.stats)
        assertEquals(emptyList<UiText>(), ui.abilities)
        assertNull(ui.cryUrl)
    }
}
