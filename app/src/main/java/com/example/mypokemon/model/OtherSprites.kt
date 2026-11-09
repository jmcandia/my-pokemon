package com.example.mypokemon.model

import com.google.gson.annotations.SerializedName

/**
 * Objeto `sprites.other` del JSON. Agrupa imágenes alternativas; la app solo usa la
 * ilustración oficial. La clave del JSON lleva guion (`official-artwork`), que no es
 * válido como nombre en Kotlin: `@SerializedName` lo resuelve.
 */
data class OtherSprites(
    @SerializedName("official-artwork")
    val officialArtwork: OfficialArtwork? = null
)
