package cl.duoc.mypokemon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import cl.duoc.mypokemon.R

/**
 * Descarga y muestra el sprite con Coil. Mientras carga muestra un spinner; si la
 * URL es `null` o falla la descarga, muestra el logo de la app.
 */
@Composable
fun PokemonImage(
    url: String?,
    name: String?,
    size: Dp
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = name,
        modifier = Modifier.size(size),
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
            }
        },
        success = {
            SubcomposeAsyncImageContent()
        },
        error = {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = stringResource(R.string.image_unavailable),
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}