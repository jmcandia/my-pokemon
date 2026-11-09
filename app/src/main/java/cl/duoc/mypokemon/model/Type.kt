package cl.duoc.mypokemon.model

import com.google.gson.annotations.SerializedName

/**
 * Tipo de un Pokémon: `{ "name": "electric", "url": "..." }`.
 * `name` viene en inglés; `PokemonTypePill` lo traduce al español.
 */
data class Type(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("url")
    val url: String? = null
)
