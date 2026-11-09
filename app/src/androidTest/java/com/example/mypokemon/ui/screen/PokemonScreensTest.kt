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
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mypokemon.R
import com.example.mypokemon.model.Pokemon
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
        types = listOf(Types(slot = 1, type = Type(name = "electric")))
    ).toUi()

    private val title = "#25 - Pikachu"
    private val buttonText get() = context.getString(R.string.random_button)

    // Cualquier spinner (CircularProgressIndicator sin progreso definido).
    private val spinner = hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)

    private fun showCompact(uiState: PokemonUiState, onRandomClick: () -> Unit = {}) {
        composeRule.setContent {
            MyPokémonTheme { PokemonCompactScreen(uiState = uiState, onRandomClick = onRandomClick) }
        }
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
                PokemonMediumScreen(uiState = PokemonUiState.Success(pikachu), onRandomClick = {})
            }
        }

        composeRule.onNodeWithText(title).assertIsDisplayed()
        composeRule.onNodeWithText(buttonText).assertIsDisplayed()
    }
}
