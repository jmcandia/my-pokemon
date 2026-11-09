package com.example.mypokemon.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mypokemon.R
import com.example.mypokemon.model.Cries
import com.example.mypokemon.model.NamedResource
import com.example.mypokemon.model.Pokemon
import com.example.mypokemon.model.PokemonAbility
import com.example.mypokemon.model.PokemonStat
import com.example.mypokemon.model.Sprites
import com.example.mypokemon.model.Type
import com.example.mypokemon.model.Types
import com.example.mypokemon.ui.components.PokemonSearchBar
import com.example.mypokemon.ui.utils.asString
import com.example.mypokemon.viewmodel.PokemonUi
import com.example.mypokemon.viewmodel.PokemonUiState
import com.example.mypokemon.viewmodel.toUi

/**
 * Layout común a todos los tamaños de pantalla: muestra la barra de búsqueda, los
 * estados de carga y error, y el botón de búsqueda aleatoria. Cada pantalla solo
 * aporta, en `successContent`, cómo se distribuye un Pokémon ya cargado.
 */
@Composable
fun PokemonStateLayout(
    uiState: PokemonUiState,
    onRandomClick: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    successContent: @Composable (PokemonUi) -> Unit
) {
    // Con scroll vertical: en pantallas bajas (teléfono en horizontal) el contenido
    // no cabe, y sin scroll el botón quedaría fuera de la pantalla.
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Arriba, con un ancho máximo para que no se estire en tablets. Se desactiva
        // mientras carga, igual que el botón aleatorio.
        PokemonSearchBar(
            enabled = uiState !is PokemonUiState.Loading,
            onSearch = onSearch,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when (uiState) {
            is PokemonUiState.Loading -> {
                val previous = uiState.previous
                if (previous == null) {
                    // Primera carga: todavía no hay nada que mostrar.
                    CircularProgressIndicator()
                } else {
                    // Mantiene el Pokémon anterior atenuado bajo el spinner, para que
                    // el layout no salte en cada búsqueda.
                    PokemonContent(previous, isLoading = true, content = successContent)
                }
            }

            is PokemonUiState.Success -> {
                PokemonContent(uiState.pokemon, isLoading = false, content = successContent)
            }

            is PokemonUiState.Error -> {
                Text(
                    text = uiState.message.asString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp, 5.dp)
                        // Los lectores de pantalla anuncian el error en cuanto aparece.
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Evento hacia arriba: la pantalla no sabe qué hace el clic, solo lo notifica.
        // Se desactiva mientras carga para no lanzar varias peticiones a la vez.
        Button(
            onClick = onRandomClick,
            enabled = uiState !is PokemonUiState.Loading
        ) {
            Text(text = stringResource(R.string.random_button))
        }
    }
}

/**
 * Contenido de un Pokémon cargado. Mientras llega el siguiente (`isLoading`), lo
 * atenúa y pone un spinner encima. Se usa el mismo contenedor en ambos casos para
 * que la pantalla no cambie de tamaño al terminar la carga.
 */
@Composable
private fun PokemonContent(
    pokemon: PokemonUi,
    isLoading: Boolean,
    content: @Composable (PokemonUi) -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.alpha(if (isLoading) 0.4f else 1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content(pokemon)
        }
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Datos fijos para que los previews se vean sin conexión a la red. Pasan por
 * `toUi()`, igual que los datos reales en el ViewModel.
 */
internal val previewPokemon = Pokemon(
    id = 25,
    name = "pikachu",
    height = 4,
    weight = 60,
    sprites = Sprites(frontDefault = null),
    types = arrayListOf(Types(slot = 1, type = Type(name = "electric"))),
    stats = listOf(
        "hp" to 35, "attack" to 55, "defense" to 40,
        "special-attack" to 50, "special-defense" to 50, "speed" to 90
    ).map { (name, value) -> PokemonStat(baseStat = value, stat = NamedResource(name = name)) },
    abilities = listOf(
        PokemonAbility(ability = NamedResource(name = "static"), isHidden = false),
        PokemonAbility(ability = NamedResource(name = "lightning-rod"), isHidden = true)
    ),
    cries = Cries(latest = "https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/25.ogg")
).toUi()
