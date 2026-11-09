package com.example.mypokemon.viewmodel

import com.example.mypokemon.R
import com.example.mypokemon.model.Pokemon
import com.example.mypokemon.ui.utils.UiText
import java.util.Locale

/**
 * Pokémon listo para mostrar: la UI lo pinta tal cual, sin formatear ni revisar
 * nulls. Lo crea el ViewModel a partir del DTO de red con `toUi()`, así la UI no
 * depende de `Pokemon` (Gson).
 */
data class PokemonUi(
    // "#25 - Pikachu", o "Pokémon desconocido" si falta el nombre.
    val title: UiText,
    // URL del artwork oficial o, si falta, del sprite básico.
    val imageUrl: String?,
    val types: List<PokemonTypeUi>,
    // Peso y altura con unidad y el separador decimal del idioma ("6,0 kg"), o "—".
    val weight: String,
    val height: String
)

/** Tipo de un Pokémon, listo para la pill. */
data class PokemonTypeUi(
    // Nombre del tipo en PokéAPI (en inglés, minúsculas). La UI elige el color con él.
    val key: String,
    // Nombre traducido, o el original capitalizado si el tipo es desconocido.
    val label: UiText
)

private const val MISSING = "—"

/**
 * Convierte el DTO de red en el modelo de UI.
 *
 * `locale` decide el separador decimal del peso y la altura. Se pasa como parámetro
 * para poder testear la conversión con un idioma fijo.
 */
internal fun Pokemon.toUi(locale: Locale = Locale.getDefault()): PokemonUi = PokemonUi(
    title = formatTitle(id, name),
    imageUrl = imageUrl,
    // Si un tipo llega sin nombre, se muestra como "normal".
    types = types.map { typeUi(it.type?.name ?: "normal") },
    weight = formatTenths(weight, "kg", locale),
    height = formatTenths(height, "m", locale)
)

/** Título "#25 - Pikachu". Maneja la falta de número, de nombre o de ambos. */
internal fun formatTitle(pokemonId: Int?, name: String?): UiText {
    val displayName = name?.replaceFirstChar { it.uppercase() }
    return when {
        pokemonId != null && displayName != null ->
            UiText.Resource(R.string.pokemon_title, listOf(pokemonId, displayName))
        pokemonId != null -> UiText.Resource(R.string.pokemon_title_unknown, listOf(pokemonId))
        displayName != null -> UiText.Plain(displayName)
        else -> UiText.Resource(R.string.pokemon_unknown)
    }
}

/**
 * PokéAPI da el peso en hectogramos y la altura en decímetros: se divide por 10 para
 * obtener kg y m. Usa el separador decimal de `locale` ("6,0 kg" en español). Si
 * falta el valor, devuelve "—".
 */
internal fun formatTenths(
    value: Int?,
    unit: String,
    locale: Locale = Locale.getDefault()
): String {
    if (value == null) return MISSING
    return String.format(locale, "%.1f %s", value / 10.0, unit)
}

/** Traduce el tipo de PokéAPI al español. Un tipo desconocido queda capitalizado. */
internal fun typeUi(type: String): PokemonTypeUi {
    val key = type.lowercase()
    val labelRes = when (key) {
        "normal" -> R.string.type_normal
        "fire" -> R.string.type_fire
        "water" -> R.string.type_water
        "electric" -> R.string.type_electric
        "grass" -> R.string.type_grass
        "ice" -> R.string.type_ice
        "fighting" -> R.string.type_fighting
        "poison" -> R.string.type_poison
        "ground" -> R.string.type_ground
        "flying" -> R.string.type_flying
        "psychic" -> R.string.type_psychic
        "bug" -> R.string.type_bug
        "rock" -> R.string.type_rock
        "ghost" -> R.string.type_ghost
        "dragon" -> R.string.type_dragon
        "dark" -> R.string.type_dark
        "steel" -> R.string.type_steel
        "fairy" -> R.string.type_fairy
        else -> null
    }
    val label = labelRes?.let { UiText.Resource(it) }
        ?: UiText.Plain(type.replaceFirstChar { it.uppercase() })
    return PokemonTypeUi(key = key, label = label)
}
