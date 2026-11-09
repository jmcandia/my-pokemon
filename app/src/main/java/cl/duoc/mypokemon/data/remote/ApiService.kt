package cl.duoc.mypokemon.data.remote

import cl.duoc.mypokemon.model.Pokemon
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Contrato de PokéAPI para Retrofit.
 *
 * Retrofit genera en tiempo de ejecución la implementación de esta interfaz:
 * cada método anotado se convierte en una petición HTTP, sin escribir código
 * de red a mano.
 */
interface ApiService {

    /**
     * `GET https://pokeapi.co/api/v2/pokemon/{idOrName}`
     *
     * - `@GET` define el método HTTP y la ruta, relativa a la `baseUrl`.
     * - `@Path` sustituye `{idOrName}` por el valor del parámetro
     *   (por ejemplo, `"25"` o `"pikachu"`).
     * - `suspend` ejecuta la llamada con corrutinas, sin bloquear el hilo principal.
     * - `Response<Pokemon>` da acceso al código HTTP y al cuerpo ya convertido por
     *   Gson. Así un 404 no lanza excepción: se revisa `isSuccessful`.
     */
    @GET("pokemon/{idOrName}")
    suspend fun getPokemon(
        @Path("idOrName") idOrName: String
    ): Response<Pokemon>
}