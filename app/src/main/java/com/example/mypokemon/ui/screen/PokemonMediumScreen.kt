package com.example.mypokemon.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.example.mypokemon.model.PokemonUiState
import com.example.mypokemon.ui.components.PokemonDetails
import com.example.mypokemon.ui.components.PokemonImage
import com.example.mypokemon.viewmodel.PokemonViewModel

@Composable
fun PokemonMediumScreen(
    modifier: Modifier,
    viewModel: PokemonViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when (val state = viewModel.uiState) {
            is PokemonUiState.Loading -> {
                CircularProgressIndicator()
            }

            is PokemonUiState.Success -> {
                val pokemon = state.pokemon

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

            is PokemonUiState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp, 5.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { viewModel.getRandomPokemon() }) {
            Text(text = "¡Buscar Pokémon aleatorio!")
        }
    }
}