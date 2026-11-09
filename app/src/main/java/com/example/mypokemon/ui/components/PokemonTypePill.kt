package com.example.mypokemon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypokemon.ui.theme.PokemonTypeColors
import com.example.mypokemon.ui.utils.asString
import com.example.mypokemon.viewmodel.PokemonTypeUi

/**
 * Etiqueta redondeada con el tipo en español y su color característico. El nombre
 * llega ya traducido desde el ViewModel; aquí solo se elige el color.
 */
@Composable
fun PokemonTypePill(
    type: PokemonTypeUi,
    modifier: Modifier = Modifier
) {
    val background = getPokemonTypeColor(type.key)

    Text(
        text = type.label.asString(),
        modifier = modifier
            .background(
                color = background,
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 6.dp
            ),
        // Blanco o negro según el fondo, para que el texto se lea bien en todos los tipos.
        color = readableTextColor(background),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Elige el color de texto (blanco o negro) con más contraste sobre `background`.
 *
 * Usa la fórmula de contraste de WCAG: `(L1 + 0.05) / (L2 + 0.05)`, donde `L` es la
 * luminancia relativa (0 = negro, 1 = blanco). La mejor de las dos opciones siempre
 * supera 4.5:1, el mínimo de WCAG AA para texto normal.
 */
internal fun readableTextColor(background: Color): Color {
    return if (contrastRatio(Color.Black, background) >= contrastRatio(Color.White, background)) {
        Color.Black
    } else {
        Color.White
    }
}

/** Relación de contraste WCAG entre dos colores, de 1:1 (iguales) a 21:1 (blanco y negro). */
internal fun contrastRatio(a: Color, b: Color): Float {
    val lighter = maxOf(a.luminance(), b.luminance())
    val darker = minOf(a.luminance(), b.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}

// Color de cada tipo (ver `PokemonTypeColors`). Un tipo desconocido se muestra en gris.
internal fun getPokemonTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "normal" -> PokemonTypeColors.Normal
        "fire" -> PokemonTypeColors.Fire
        "water" -> PokemonTypeColors.Water
        "electric" -> PokemonTypeColors.Electric
        "grass" -> PokemonTypeColors.Grass
        "ice" -> PokemonTypeColors.Ice
        "fighting" -> PokemonTypeColors.Fighting
        "poison" -> PokemonTypeColors.Poison
        "ground" -> PokemonTypeColors.Ground
        "flying" -> PokemonTypeColors.Flying
        "psychic" -> PokemonTypeColors.Psychic
        "bug" -> PokemonTypeColors.Bug
        "rock" -> PokemonTypeColors.Rock
        "ghost" -> PokemonTypeColors.Ghost
        "dragon" -> PokemonTypeColors.Dragon
        "dark" -> PokemonTypeColors.Dark
        "steel" -> PokemonTypeColors.Steel
        "fairy" -> PokemonTypeColors.Fairy
        else -> PokemonTypeColors.Unknown
    }
}