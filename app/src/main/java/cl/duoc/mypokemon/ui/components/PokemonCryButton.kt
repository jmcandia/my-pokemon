package cl.duoc.mypokemon.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.duoc.mypokemon.R

/**
 * Botón que reproduce el grito del Pokémon (un `.ogg` de PokéAPI) con `MediaPlayer`.
 *
 * Reproducir audio es un efecto de la UI, como descargar la imagen con Coil, así que
 * vive aquí y no en el ViewModel. El reproductor se libera al cambiar de Pokémon o
 * al salir de la pantalla, para no dejar recursos de audio abiertos.
 */
@Composable
fun PokemonCryButton(url: String, modifier: Modifier = Modifier) {
    val player = remember { mutableStateOf<MediaPlayer?>(null) }

    // `onDispose` se ejecuta si cambia `url` (otro Pokémon) o si el botón desaparece.
    DisposableEffect(url) {
        onDispose {
            player.value?.release()
            player.value = null
        }
    }

    TextButton(
        onClick = {
            // Un clic mientras suena reinicia el grito desde el principio.
            player.value?.release()
            player.value = playCry(url)
        },
        modifier = modifier
    ) {
        Text(text = "▶ " + stringResource(R.string.play_cry))
    }
}

/**
 * Crea un `MediaPlayer` que descarga y reproduce `url`. `prepareAsync` descarga en
 * segundo plano y `start` se llama al terminar, sin bloquear la UI. Devuelve `null`
 * si la URL no se puede abrir: el botón simplemente no suena.
 */
private fun playCry(url: String): MediaPlayer? {
    val player = MediaPlayer()
    return try {
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        player.setDataSource(url)
        player.setOnPreparedListener { it.start() }
        // Un error de reproducción (sin red, formato inválido) se ignora: devolver
        // `true` indica que está manejado y evita que Android lo trate como fatal.
        player.setOnErrorListener { _, _, _ -> true }
        player.prepareAsync()
        player
    } catch (e: Exception) {
        // `setDataSource` lanza `IOException` o `IllegalArgumentException` si la URL
        // no es válida. Se libera el reproductor para no dejarlo abierto.
        player.release()
        null
    }
}
