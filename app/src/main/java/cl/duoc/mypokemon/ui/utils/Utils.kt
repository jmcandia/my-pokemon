package cl.duoc.mypokemon.ui.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

/**
 * Calcula la clase de tamaño de ventana (Compact, Medium o Expanded) de la Activity
 * actual. Necesita una Activity real, por eso no funciona en los previews.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun getWindowSizeClass(): WindowSizeClass{
    return calculateWindowSizeClass(LocalActivity.current as android.app.Activity)
}