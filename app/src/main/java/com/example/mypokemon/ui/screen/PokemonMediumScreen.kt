package com.example.mypokemon.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mypokemon.ui.components.PokemonDetails
import com.example.mypokemon.ui.components.PokemonImage
import com.example.mypokemon.ui.utils.asString
import com.example.mypokemon.viewmodel.PokemonUiState

/**
 * Layout para pantallas medianas y anchas (tablet, plegable, teléfono en horizontal):
 * detalles a la izquierda e imagen a la derecha, centrados como grupo. No conoce el
 * ViewModel: recibe el estado y las acciones
 * como parámetros.
 */
@Composable
fun PokemonMediumScreen(
    uiState: PokemonUiState,
    onRandomClick: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PokemonStateLayout(
        uiState = uiState,
        onRandomClick = onRandomClick,
        onSearch = onSearch,
        modifier = modifier
    ) { pokemon ->
        // Detalles a la izquierda e imagen a la derecha. Sin pesos, la fila ocupa
        // solo lo que mide su contenido y `CenterHorizontally` centra el grupo en la
        // pantalla, con 48 dp de separación entre ambos.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PokemonDetails(pokemon = pokemon)

            PokemonImage(
                url = pokemon.imageUrl,
                name = pokemon.title.asString(),
                size = 200.dp
            )
        }
    }
}

// Preview con tamaño de tablet en horizontal y datos fijos.
@Preview(showBackground = true, widthDp = 840, heightDp = 480)
@Composable
fun PokemonMediumScreenPreview() {
    PokemonMediumScreen(
        uiState = PokemonUiState.Success(previewPokemon),
        onRandomClick = {},
        onSearch = {}
    )
}
