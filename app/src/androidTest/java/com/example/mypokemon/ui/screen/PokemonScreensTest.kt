package com.example.mypokemon.ui.screen

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mypokemon.R
import com.example.mypokemon.model.Cries
import com.example.mypokemon.model.NamedResource
import com.example.mypokemon.model.Pokemon
import com.example.mypokemon.model.PokemonAbility
import com.example.mypokemon.model.PokemonStat
import com.example.mypokemon.model.Type
import com.example.mypokemon.model.Types
import com.example.mypokemon.ui.theme.MyPokémonTheme
import com.example.mypokemon.ui.utils.UiText
import com.example.mypokemon.viewmodel.PokemonUiState
import com.example.mypokemon.viewmodel.toUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI de las pantallas. Como no conocen el ViewModel, se les pasa un estado
 * fijo y se comprueba qué muestran: no hace falta red ni repositorio.
 *
 * Los textos se leen de los recursos (`context.getString`) en lugar de escribirlos,
 * para que los tests pasen con el dispositivo en español o en inglés.
 */
@RunWith(AndroidJUnit4::class)
class PokemonScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val pikachu = Pokemon(
        id = 25,
        name = "pikachu",
        height = 4,
        weight = 60,
        types = listOf(Types(slot = 1, type = Type(name = "electric"))),
        stats = listOf(PokemonStat(baseStat = 90, stat = NamedResource(name = "speed"))),
        abilities = listOf(
            PokemonAbility(ability = NamedResource(name = "lightning-rod"), isHidden = true)
        ),
        cries = Cries(latest = "https://example.com/25.ogg")
    ).toUi()

    private val title = "#25 - Pikachu"
    private val buttonText get() = context.getString(R.string.random_button)

    // Cualquier spinner (CircularProgressIndicator sin progreso definido).
    private val spinner = hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)

    private val searchLabel get() = context.getString(R.string.search_label)
    private val searchButton get() = context.getString(R.string.search_button)

    private fun showCompact(
        uiState: PokemonUiState,
        onRandomClick: () -> Unit = {},
        onSearch: (String) -> Unit = {}
    ) {
        composeRule.setContent {
            MyPokémonTheme {
                PokemonCompactScreen(
                    uiState = uiState,
                    onRandomClick = onRandomClick,
                    onSearch = onSearch
                )
            }
        }
    }

    @Test
    fun muestra_grito_habilidades_y_estadisticas() {
        showCompact(PokemonUiState.Success(pikachu))

        composeRule.onNodeWithText(context.getString(R.string.play_cry), substring = true)
            .assertIsDisplayed()
        val abilities = context.getString(
            R.string.abilities_label,
            context.getString(R.string.ability_hidden, "Lightning rod")
        )
        composeRule.onNodeWithText(abilities).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_title)).assertExists()
        // La fila de la estadística se lee entera: nombre y valor juntos.
        composeRule.onNode(
            hasText(context.getString(R.string.stat_speed)) and hasText("90")
        ).assertExists()
    }

    @Test
    fun buscar_envia_el_texto_escrito() {
        val searches = mutableListOf<String>()
        showCompact(PokemonUiState.Success(pikachu), onSearch = { searches += it })

        composeRule.onNodeWithText(searchLabel).performTextInput("Mr. Mime")
        composeRule.onNodeWithText(searchButton).performClick()

        // La pantalla envía el texto tal cual; normalizarlo es tarea del ViewModel.
        assertEquals(listOf("Mr. Mime"), searches)
    }

    @Test
    fun el_boton_buscar_se_desactiva_sin_texto_y_durante_la_carga() {
        showCompact(PokemonUiState.Success(pikachu))
        composeRule.onNodeWithText(searchButton).assertIsNotEnabled()

        composeRule.onNodeWithText(searchLabel).performTextInput("pikachu")
        composeRule.onNodeWithText(searchButton).assertIsEnabled()
    }

    @Test
    fun durante_la_carga_no_se_puede_buscar() {
        showCompact(PokemonUiState.Loading())

        composeRule.onNodeWithText(searchLabel).performTextInput("pikachu")
        composeRule.onNodeWithText(searchButton).assertIsNotEnabled()
    }

    @Test
    fun exito_muestra_titulo_tipo_peso_y_boton_activo() {
        showCompact(PokemonUiState.Success(pikachu))

        composeRule.onNodeWithText(title).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.type_electric)).assertIsDisplayed()
        val weight = context.getString(
            R.string.pokemon_weight,
            context.getString(R.string.weight_value, 6.0)
        )
        composeRule.onNodeWithText(weight).assertIsDisplayed()
        composeRule.onNodeWithText(buttonText).assertIsEnabled()
    }

    @Test
    fun el_titulo_es_un_encabezado_para_lectores_de_pantalla() {
        showCompact(PokemonUiState.Success(pikachu))

        composeRule.onNode(isHeading()).assertTextEquals(title)
    }

    @Test
    fun primera_carga_muestra_solo_spinner_y_desactiva_el_boton() {
        showCompact(PokemonUiState.Loading())

        composeRule.onNode(spinner).assertIsDisplayed()
        composeRule.onNodeWithText(title).assertDoesNotExist()
        composeRule.onNodeWithText(buttonText).assertIsNotEnabled()
    }

    @Test
    fun carga_con_pokemon_previo_lo_mantiene_bajo_el_spinner() {
        showCompact(PokemonUiState.Loading(previous = pikachu))

        composeRule.onNodeWithText(title).assertIsDisplayed()
        // Puede haber más de un spinner (el de la imagen de Coil), basta con uno.
        composeRule.onAllNodes(spinner).onFirst().assertIsDisplayed()
        composeRule.onNodeWithText(buttonText).assertIsNotEnabled()
    }

    @Test
    fun error_muestra_mensaje_y_el_boton_permite_reintentar() {
        var clicks = 0
        showCompact(
            PokemonUiState.Error(UiText.Resource(R.string.error_network)),
            onRandomClick = { clicks++ }
        )

        composeRule.onNodeWithText(context.getString(R.string.error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(buttonText).assertIsEnabled().performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun el_error_se_anuncia_a_los_lectores_de_pantalla() {
        showCompact(PokemonUiState.Error(UiText.Resource(R.string.error_server, listOf(500))))

        composeRule.onNodeWithText(context.getString(R.string.error_server, 500))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test
    fun pantalla_mediana_muestra_el_pokemon_y_el_boton() {
        composeRule.setContent {
            MyPokémonTheme {
                PokemonMediumScreen(
                    uiState = PokemonUiState.Success(pikachu),
                    onRandomClick = {},
                    onSearch = {}
                )
            }
        }

        composeRule.onNodeWithText(title).assertIsDisplayed()
        composeRule.onNodeWithText(buttonText).assertIsDisplayed()
    }
}
