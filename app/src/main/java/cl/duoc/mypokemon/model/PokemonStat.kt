package cl.duoc.mypokemon.model

import com.google.gson.annotations.SerializedName

/**
 * Elemento de la lista `stats`: `{"base_stat": 35, "stat": {"name": "hp", …}}`.
 * Cada Pokémon tiene seis (PS, ataque, defensa, ataque y defensa especiales y
 * velocidad). Nullable, como el resto de los DTOs, porque Gson no respeta la
 * nulabilidad de Kotlin.
 */
data class PokemonStat(
    @SerializedName("base_stat")
    val baseStat: Int? = null,
    @SerializedName("stat")
    val stat: NamedResource? = null
)
