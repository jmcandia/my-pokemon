package com.example.mypokemon.model

import com.google.gson.annotations.SerializedName

/**
 * Elemento de la lista `types` del JSON: `{ "slot": 1, "type": { ... } }`.
 * `slot` es el orden del tipo (1 = principal, 2 = secundario).
 */
data class Types(
    @SerializedName("slot")
    val slot: Int? = null,
    @SerializedName("type")
    val type: Type? = Type()
)
