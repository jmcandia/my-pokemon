package com.example.mypokemon.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mypokemon.R
import com.example.mypokemon.ui.utils.getWindowSizeClass
import com.example.mypokemon.viewmodel.PokemonViewModel

/**
 * Pantalla principal. Es el único composable que conoce el ViewModel: lee su estado
 * y se lo pasa a las pantallas, junto con la acción para pedir otro Pokémon.
 *
 * `viewModel(factory = ...)` reutiliza el mismo ViewModel tras una rotación; la
 * `Factory` le inyecta el repositorio real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonScreen(
    viewModel: PokemonViewModel = viewModel(factory = PokemonViewModel.Factory)
) {
    // Convierte el `StateFlow` en estado de Compose. Deja de recolectar cuando la app
    // pasa a segundo plano y vuelve a hacerlo al regresar.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            // Decorativo: el título de al lado ya dice el nombre de la
                            // app, así que los lectores de pantalla lo saltan.
                            contentDescription = null,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        // Elige el layout según el ancho de la ventana (teléfono, plegable o tablet).
        val windowSizeClass = getWindowSizeClass()
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> {
                PokemonCompactScreen(
                    uiState = uiState,
                    onRandomClick = viewModel::getRandomPokemon,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            WindowWidthSizeClass.Medium,
            WindowWidthSizeClass.Expanded -> {
                PokemonMediumScreen(
                    uiState = uiState,
                    onRandomClick = viewModel::getRandomPokemon,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
