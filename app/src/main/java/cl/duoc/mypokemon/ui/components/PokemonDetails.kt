package cl.duoc.mypokemon.ui.components

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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cl.duoc.mypokemon.R
import cl.duoc.mypokemon.ui.utils.asString
import cl.duoc.mypokemon.viewmodel.PokemonUi

/**
 * Muestra número, nombre, grito, tipos, peso, altura, habilidades y estadísticas
 * del Pokémon. Los datos llegan ya
 * formateados desde el ViewModel (`PokemonUi`): aquí solo se distribuyen.
 */
@Composable
fun PokemonDetails(
    pokemon: PokemonUi,
    // La pantalla mediana pone las estadísticas fuera, debajo de la fila; las demás
    // las muestran aquí, al final de los detalles.
    showStats: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = pokemon.title.asString(),
            // Encabezado: los lectores de pantalla permiten saltar directo a él.
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium
        )

        pokemon.cryUrl?.let { PokemonCryButton(url = it) }

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
            Text(text = stringResource(R.string.pokemon_weight, pokemon.weight.asString()))
            Text(text = stringResource(R.string.pokemon_height, pokemon.height.asString()))
        }

        if (pokemon.abilities.isNotEmpty()) {
            // `map` es inline, así que se puede llamar a `asString()` (composable) dentro.
            val abilities = pokemon.abilities.map { it.asString() }.joinToString(", ")
            Text(text = stringResource(R.string.abilities_label, abilities))
        }

        if (showStats) {
            PokemonStats(stats = pokemon.stats, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
