package com.example.mypokemon.ui.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mypokemon.ui.components.PokemonDetails
import com.example.mypokemon.ui.components.PokemonImage
import com.example.mypokemon.ui.utils.asString
import com.example.mypokemon.viewmodel.PokemonUiState

/**
 * Layout para pantallas estrechas (teléfono en vertical): imagen arriba y detalles
 * debajo. No conoce el ViewModel: recibe el estado y la acción como parámetros.
 */
@Composable
fun PokemonCompactScreen(
    uiState: PokemonUiState,
    onRandomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PokemonStateLayout(
        uiState = uiState,
        onRandomClick = onRandomClick,
        modifier = modifier
    ) { pokemon ->
        PokemonImage(
            url = pokemon.imageUrl,
            name = pokemon.title.asString(),
            size = 200.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        PokemonDetails(pokemon = pokemon)
    }
}

// Preview con datos fijos: no necesita red ni ViewModel.
@Preview(showBackground = true)
@Composable
fun PokemonCompactScreenPreview() {
    PokemonCompactScreen(
        uiState = PokemonUiState.Success(previewPokemon),
        onRandomClick = {}
    )
}
