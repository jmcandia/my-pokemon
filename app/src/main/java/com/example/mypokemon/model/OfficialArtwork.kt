package com.example.mypokemon.model

import com.google.gson.annotations.SerializedName

/** Objeto `sprites.other.official-artwork`: ilustración oficial (unos 475 px). */
data class OfficialArtwork(
    @SerializedName("front_default")
    val frontDefault: String? = null
)
