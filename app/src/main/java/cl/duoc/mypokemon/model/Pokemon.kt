package cl.duoc.mypokemon.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de `GET pokemon/{idOrName}`. Solo se declaran los campos que usa la
 * app: Gson ignora el resto del JSON.
 *
 * `@SerializedName` indica el nombre de la clave en el JSON. Todos los parámetros
 * tienen valor por defecto: así Kotlin genera un constructor vacío y Gson conserva
 * esos valores para las claves que falten. Son nullable porque Gson no respeta la
 * nulabilidad de Kotlin: un `null` explícito en el JSON se asigna tal cual.
 */
data class Pokemon(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    // En decímetros. `toUi()` lo divide por 10 para mostrar metros.
    @SerializedName("height")
    val height: Int? = null,
    // En hectogramos. `toUi()` lo divide por 10 para mostrar kilogramos.
    @SerializedName("weight")
    val weight: Int? = null,
    @SerializedName("sprites")
    val sprites: Sprites? = Sprites(),
    // Nullable como el resto: con `"types": null` en el JSON, Gson asignaría `null`
    // aunque el tipo dijera lo contrario, y la app fallaría al recorrer la lista.
    @SerializedName("types")
    val types: List<Types>? = null,
    @SerializedName("stats")
    val stats: List<PokemonStat>? = null,
    @SerializedName("abilities")
    val abilities: List<PokemonAbility>? = null,
    @SerializedName("cries")
    val cries: Cries? = null
) {
    /**
     * URL de la imagen a mostrar: la ilustración oficial si existe y, si no, el
     * sprite pixel art. Es una propiedad calculada (sin campo), así que Gson la ignora.
     */
    val imageUrl: String?
        get() = sprites?.other?.officialArtwork?.frontDefault ?: sprites?.frontDefault
}