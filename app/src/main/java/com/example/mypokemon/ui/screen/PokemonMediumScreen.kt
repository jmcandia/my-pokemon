package com.example.mypokemon.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mypokemon.model.PokemonUiState
import com.example.mypokemon.ui.components.PokemonDetails
import com.example.mypokemon.ui.components.PokemonImage

@Composable
fun PokemonMediumScreen(
    uiState: PokemonUiState,
    onRandomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PokemonStateLayout(
        uiState = uiState,
        onRandomClick = onRandomClick,
        modifier = modifier
    ) { pokemon ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                PokemonImage(
                    url = pokemon.sprites?.frontDefault,
                    name = pokemon.name,
                    size = 200.dp
                )
            }

            Box(
                modifier = Modifier.weight(1.5f),
                contentAlignment = Alignment.Center
            ) {
                PokemonDetails(
                    pokemonId = pokemon.id,
                    name = pokemon.name,
                    types = pokemon.types,
                    weight = pokemon.weight,
                    height = pokemon.height
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 840, heightDp = 480)
@Composable
fun PokemonMediumScreenPreview() {
    PokemonMediumScreen(
        uiState = PokemonUiState.Success(previewPokemon),
        onRandomClick = {}
    )
}
