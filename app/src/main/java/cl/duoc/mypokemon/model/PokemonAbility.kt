package cl.duoc.mypokemon.model

import com.google.gson.annotations.SerializedName

/**
 * Elemento de la lista `abilities`:
 * `{"is_hidden": true, "ability": {"name": "lightning-rod", …}}`.
 * Una habilidad oculta solo se obtiene por medios especiales en los juegos.
 */
data class PokemonAbility(
    @SerializedName("ability")
    val ability: NamedResource? = null,
    @SerializedName("is_hidden")
    val isHidden: Boolean? = null
)
