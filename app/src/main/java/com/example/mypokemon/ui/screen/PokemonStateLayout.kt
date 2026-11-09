package com.example.mypokemon.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mypokemon.R
import com.example.mypokemon.model.Pokemon
import com.example.mypokemon.model.Sprites
import com.example.mypokemon.model.Type
import com.example.mypokemon.model.Types
import com.example.mypokemon.ui.utils.asString
import com.example.mypokemon.viewmodel.PokemonUi
import com.example.mypokemon.viewmodel.PokemonUiState
import com.example.mypokemon.viewmodel.toUi

/**
 * Layout común a todos los tamaños de pantalla: muestra los estados de carga y
 * error, y el botón de búsqueda aleatoria. Cada pantalla solo aporta, en
 * `successContent`, cómo se distribuye un Pokémon ya cargado.
 */
@Composable
fun PokemonStateLayout(
    uiState: PokemonUiState,
    onRandomClick: () -> Unit,
    modifier: Modifier = Modifier,
    successContent: @Composable (PokemonUi) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when (uiState) {
            is PokemonUiState.Loading -> {
                CircularProgressIndicator()
            }

            is PokemonUiState.Success -> {
                successContent(uiState.pokemon)
            }

            is PokemonUiState.Error -> {
                Text(
                    text = uiState.message.asString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp, 5.dp),
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
 * Datos fijos para que los previews se vean sin conexión a la red. Pasan por
 * `toUi()`, igual que los datos reales en el ViewModel.
 */
internal val previewPokemon = Pokemon(
    id = 25,
    name = "pikachu",
    height = 4,
    weight = 60,
    sprites = Sprites(frontDefault = null),
    types = arrayListOf(Types(slot = 1, type = Type(name = "electric")))
).toUi()
