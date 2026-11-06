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
import androidx.compose.ui.unit.dp
import com.example.mypokemon.model.Types

/** Muestra número, nombre, tipos, peso y altura del Pokémon. */
@Composable
fun PokemonDetails(
    pokemonId: Int?,
    name: String?,
    types: List<Types>,
    weight: Int?,
    height: Int?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "#${pokemonId} - ${name?.replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            types.forEach { type ->
                PokemonTypePill(
                    type = type.type?.name ?: "normal",
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PokéAPI usa hectogramos y decímetros: se divide por 10 para kg y m.
            Text(text = "Peso: ${weight?.div(10.0)} kg")
            Text(text = "Altura: ${height?.div(10.0)} m")
        }
    }
}