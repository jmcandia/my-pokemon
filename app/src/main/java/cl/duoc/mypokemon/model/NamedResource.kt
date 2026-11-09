package cl.duoc.mypokemon.model

import com.google.gson.annotations.SerializedName

/**
 * Referencia de PokéAPI a otro recurso: `{"name": "attack", "url": "…/stat/2/"}`.
 * La usan las estadísticas y las habilidades. `name` es un identificador en inglés,
 * en minúsculas y con guiones (`special-attack`, `lightning-rod`).
 */
data class NamedResource(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("url")
    val url: String? = null
)
