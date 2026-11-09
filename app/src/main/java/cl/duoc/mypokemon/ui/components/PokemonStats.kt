package cl.duoc.mypokemon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.mypokemon.R
import cl.duoc.mypokemon.ui.utils.asString
import cl.duoc.mypokemon.viewmodel.PokemonStatUi

/**
 * Estadísticas base: una fila por estadística con nombre, valor y barra. Las barras
 * llegan ya calculadas (`fraction`) desde el ViewModel. Si no hay estadísticas, no
 * muestra nada.
 */
@Composable
fun PokemonStats(stats: List<PokemonStatUi>, modifier: Modifier = Modifier) {
    if (stats.isEmpty()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.stats_title),
            style = MaterialTheme.typography.titleMedium
        )
        stats.forEach { stat ->
            // Los lectores de pantalla leen la fila entera ("Ataque 55") como un solo
            // elemento, en lugar de tres.
            Row(
                modifier = Modifier.semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.label.asString(),
                    modifier = Modifier.width(88.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stat.value.toString(),
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                LinearProgressIndicator(
                    progress = { stat.fraction },
                    // La barra es decorativa: el valor ya se lee en el texto. Sin esto,
                    // el lector de pantalla diría además un porcentaje confuso.
                    modifier = Modifier
                        .width(140.dp)
                        .clearAndSetSemantics {}
                )
            }
        }
    }
}
