package cl.duoc.mypokemon.model

import com.google.gson.annotations.SerializedName

/**
 * URLs de las imágenes del Pokémon. El JSON usa snake_case (`front_default`) y
 * `@SerializedName` lo mapea a camelCase (`frontDefault`).
 *
 * `frontDefault` es un sprite pixel art de 96 px. La app prefiere la ilustración
 * oficial de `other`, de mayor resolución (ver `Pokemon.imageUrl`).
 */
data class Sprites(
    @SerializedName("back_shiny")
    val backShiny: String? = null,
    @SerializedName("back_female")
    val backFemale: String? = null,
    @SerializedName("front_shiny")
    val frontShiny: String? = null,
    @SerializedName("back_default")
    val backDefault: String? = null,
    @SerializedName("front_female")
    val frontFemale: String? = null,
    @SerializedName("front_default")
    val frontDefault: String? = null,
    @SerializedName("back_shiny_female")
    val backShinyFemale: String? = null,
    @SerializedName("front_shiny_female")
    val frontShinyFemale: String? = null,
    @SerializedName("other")
    val other: OtherSprites? = null
)
