package com.example.mypokemon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mypokemon.R
import com.example.mypokemon.ui.utils.asString
import com.example.mypokemon.viewmodel.PokemonUi

/**
 * Muestra número, nombre, tipos, peso y altura del Pokémon. Los datos llegan ya
 * formateados desde el ViewModel (`PokemonUi`): aquí solo se distribuyen.
 */
@Composable
fun PokemonDetails(pokemon: PokemonUi) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = pokemon.title.asString(),
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pokemon.types.forEach { type ->
                PokemonTypePill(
                    type = type,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.pokemon_weight, pokemon.weight))
            Text(text = stringResource(R.string.pokemon_height, pokemon.height))
        }
    }
}
