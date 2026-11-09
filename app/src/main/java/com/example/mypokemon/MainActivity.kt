package com.example.mypokemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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