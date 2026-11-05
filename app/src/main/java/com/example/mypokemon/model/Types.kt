package com.example.mypokemon.model

import com.google.gson.annotations.SerializedName

data class Types(
    @SerializedName("slot")
    val slot: Int? = null,
    @SerializedName("type")
    val type: Type? = Type()
)
