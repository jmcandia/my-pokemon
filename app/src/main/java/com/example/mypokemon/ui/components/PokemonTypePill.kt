package com.example.mypokemon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mypokemon.ui.theme.PokemonTypeColors

@Composable
fun PokemonTypePill(
    type: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = translatePokemonType(type),
        modifier = modifier
            .background(
                color = getPokemonTypeColor(type),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 6.dp
            ),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

private fun translatePokemonType(type: String): String {
    return when (type.lowercase()) {
        "normal" -> "Normal"
        "fire" -> "Fuego"
        "water" -> "Agua"
        "electric" -> "Eléctrico"
        "grass" -> "Planta"
        "ice" -> "Hielo"
        "fighting" -> "Lucha"
        "poison" -> "Veneno"
        "ground" -> "Tierra"
        "flying" -> "Volador"
        "psychic" -> "Psíquico"
        "bug" -> "Bicho"
        "rock" -> "Roca"
        "ghost" -> "Fantasma"
        "dragon" -> "Dragón"
        "dark" -> "Siniestro"
        "steel" -> "Acero"
        "fairy" -> "Hada"
        else -> type.replaceFirstChar { it.uppercase() }
    }
}

private fun getPokemonTypeColor(type: String): Color {
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