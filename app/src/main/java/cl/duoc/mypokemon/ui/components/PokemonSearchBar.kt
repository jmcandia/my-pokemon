package cl.duoc.mypokemon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cl.duoc.mypokemon.R

/**
 * Campo para buscar un Pokémon por nombre o número, con su botón.
 *
 * El texto que se está escribiendo es estado de la UI, no del ViewModel: se guarda
 * con `rememberSaveable` (sobrevive a la rotación) y solo se envía al pulsar
 * "Buscar" o la tecla de búsqueda del teclado. La normalización (minúsculas, tildes,
 * ceros) la hace el ViewModel.
 */
@Composable
fun PokemonSearchBar(
    enabled: Boolean,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val canSearch = enabled && query.isNotBlank()

    val submit: () -> Unit = {
        if (canSearch) {
            keyboard?.hide()
            onSearch(query)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(text = stringResource(R.string.search_label)) },
            singleLine = true,
            // La tecla de acción del teclado muestra una lupa y lanza la búsqueda.
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submit() }),
            modifier = Modifier.weight(1f)
        )
        Button(onClick = submit, enabled = canSearch) {
            Text(text = stringResource(R.string.search_button))
        }
    }
}
