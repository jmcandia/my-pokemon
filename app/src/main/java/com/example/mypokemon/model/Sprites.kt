package com.example.mypokemon.model

import com.google.gson.annotations.SerializedName

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
    val frontShinyFemale: String? = null
)
