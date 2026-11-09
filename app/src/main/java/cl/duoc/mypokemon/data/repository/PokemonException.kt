package cl.duoc.mypokemon.data.repository

/**
 * Errores que puede lanzar `PokemonRepository`.
 *
 * Ocultan la fuente de datos: el ViewModel no sabe si el fallo vino de Retrofit,
 * de OkHttp o de Gson, solo de qué tipo es. Al ser `sealed`, el `when` que los
 * traduce debe cubrir los tres casos o no compila.
 */
sealed class PokemonException(cause: Throwable? = null) : Exception(cause) {
    // Sin conexión, timeout o fallo de DNS.
    class Network(cause: Throwable) : PokemonException(cause)

    // No existe un Pokémon con ese nombre o número (el servidor respondió 404).
    class NotFound : PokemonException()

    // El servidor respondió con otro error (500…) o sin cuerpo. Guarda el código HTTP.
    class Server(val code: Int) : PokemonException()

    // Cualquier otro fallo, por ejemplo un JSON que Gson no puede leer.
    class Unknown(cause: Throwable) : PokemonException(cause)
}
