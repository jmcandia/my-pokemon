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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mypokemon.R
import com.example.mypokemon.ui.utils.getWindowSizeClass
import com.example.mypokemon.viewmodel.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonScreen(
    viewModel: PokemonViewModel = viewModel(factory = PokemonViewModel.Factory)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "My Pokémon",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val windowSizeClass = getWindowSizeClass()
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> {
                PokemonCompactScreen(
                    uiState = viewModel.uiState,
                    onRandomClick = viewModel::getRandomPokemon,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            WindowWidthSizeClass.Medium,
            WindowWidthSizeClass.Expanded -> {
                PokemonMediumScreen(
                    uiState = viewModel.uiState,
                    onRandomClick = viewModel::getRandomPokemon,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
