package cl.duoc.mypokemon.model

import com.google.gson.annotations.SerializedName

/**
 * Objeto `cries`: URLs del grito del Pokémon en formato `.ogg`. `latest` es la
 * versión de los juegos recientes; se ignora `legacy` (la de los juegos antiguos).
 */
data class Cries(
    @SerializedName("latest")
    val latest: String? = null
)
