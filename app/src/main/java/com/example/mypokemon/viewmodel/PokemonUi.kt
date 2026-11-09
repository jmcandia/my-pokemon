package com.example.mypokemon.viewmodel

import androidx.annotation.StringRes
import com.example.mypokemon.R
import com.example.mypokemon.model.Pokemon
import com.example.mypokemon.ui.utils.UiText

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
    // Peso y altura ("6,0 kg"), o "—". Son `UiText` y no `String` para que el número
    // se formatee al dibujar, con el idioma vigente: si el usuario cambia el idioma,
    // el ViewModel sobrevive pero el separador decimal se actualiza.
    val weight: UiText,
    val height: UiText,
    // Estadísticas base, en el orden de PokéAPI (PS, ataque, defensa…).
    val stats: List<PokemonStatUi> = emptyList(),
    // Habilidades: "Static", "Lightning rod (oculta)".
    val abilities: List<UiText> = emptyList(),
    // URL del grito (.ogg), o null si PokéAPI no lo tiene.
    val cryUrl: String? = null
)

/** Estadística base lista para la barra. */
data class PokemonStatUi(
    // Nombre traducido ("Ataque"), o el original formateado si es desconocida.
    val label: UiText,
    val value: Int,
    // Proporción de la barra, de 0 a 1, respecto al máximo `MAX_BASE_STAT`.
    val fraction: Float
)

/** Tipo de un Pokémon, listo para la pill. */
data class PokemonTypeUi(
    // Nombre del tipo en PokéAPI (en inglés, minúsculas). La UI elige el color con él.
    val key: String,
    // Nombre traducido, o el original capitalizado si el tipo es desconocido.
    val label: UiText
)

private const val MISSING = "—"

// Estadística base más alta de todos los Pokémon (los PS de Blissey). Las barras se
// dibujan respecto a este valor, así son comparables entre Pokémon.
internal const val MAX_BASE_STAT = 255

/** Convierte el DTO de red en el modelo de UI. */
internal fun Pokemon.toUi(): PokemonUi = PokemonUi(
    title = formatTitle(id, name),
    imageUrl = imageUrl,
    // Sin lista de tipos no se muestra ninguno. Si un tipo llega sin nombre, se
    // muestra como "normal".
    types = types.orEmpty().map { typeUi(it.type?.name ?: "normal") },
    weight = formatTenths(weight, R.string.weight_value),
    height = formatTenths(height, R.string.height_value),
    // Una estadística sin valor no se puede dibujar: se omite.
    stats = stats.orEmpty().mapNotNull { stat ->
        stat.baseStat?.let { statUi(stat.stat?.name, it) }
    },
    // Una habilidad sin nombre no aporta nada: se omite.
    abilities = abilities.orEmpty().mapNotNull { ability ->
        ability.ability?.name?.let { abilityUi(it, ability.isHidden == true) }
    },
    cryUrl = cries?.latest
)

/**
 * Convierte un identificador de PokéAPI en texto legible: `lightning-rod` →
 * "Lightning rod".
 */
internal fun formatSlug(slug: String): String =
    slug.replace('-', ' ').replaceFirstChar { it.uppercase() }

/** Traduce la estadística y calcula la proporción de su barra. */
internal fun statUi(name: String?, value: Int): PokemonStatUi {
    val labelRes = when (name) {
        "hp" -> R.string.stat_hp
        "attack" -> R.string.stat_attack
        "defense" -> R.string.stat_defense
        "special-attack" -> R.string.stat_special_attack
        "special-defense" -> R.string.stat_special_defense
        "speed" -> R.string.stat_speed
        else -> null
    }
    val label = labelRes?.let { UiText.Resource(it) }
        ?: UiText.Plain(name?.let(::formatSlug) ?: MISSING)
    val fraction = (value.toFloat() / MAX_BASE_STAT).coerceIn(0f, 1f)
    return PokemonStatUi(label = label, value = value, fraction = fraction)
}

/**
 * Nombre legible de la habilidad. PokéAPI solo da el identificador en inglés;
 * traducirlo exigiría una petición más por habilidad, así que se muestra
 * formateado. Si es oculta, se marca con "(oculta)".
 */
internal fun abilityUi(name: String, isHidden: Boolean): UiText {
    val displayName = formatSlug(name)
    return if (isHidden) {
        UiText.Resource(R.string.ability_hidden, listOf(displayName))
    } else {
        UiText.Plain(displayName)
    }
}

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
 * obtener kg y m. `formatRes` es un recurso con formato `%1$.1f`: al resolverlo, la
 * UI usa el separador decimal del idioma ("6,0 kg" en español). Si falta el valor,
 * devuelve "—".
 */
internal fun formatTenths(value: Int?, @StringRes formatRes: Int): UiText {
    if (value == null) return UiText.Plain(MISSING)
    return UiText.Resource(formatRes, listOf(value / 10.0))
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
