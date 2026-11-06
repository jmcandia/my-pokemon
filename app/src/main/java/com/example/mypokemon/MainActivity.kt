package com.example.mypokemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mypokemon.ui.screen.PokemonScreen
import com.example.mypokemon.ui.theme.MyPokémonTheme

/** Única Activity de la app: aplica el tema y monta la pantalla principal. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dibuja detrás de las barras del sistema; el `Scaffold` aplica los márgenes.
        enableEdgeToEdge()
        setContent {
            MyPokémonTheme {
                PokemonScreen()
            }
        }
    }
}