package cl.duoc.mypokemon.data.repository

import cl.duoc.mypokemon.data.remote.ApiService
import cl.duoc.mypokemon.model.Pokemon
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.HttpURLConnection.HTTP_NOT_FOUND

/**
 * Implementación de `PokemonRepository` que pide los datos a PokéAPI.
 *
 * Recibe `ApiService` por constructor (inyección de dependencias). En la app se le
 * pasa el `ApiService` que crea `AppContainer`; en un test, una API falsa.
 */
class DefaultPokemonRepository(
    private val api: ApiService
) : PokemonRepository {

    /**
     * Pide el Pokémon a la API y traduce cada fallo a una `PokemonException`, para
     * que el ViewModel no dependa de Retrofit.
     */
    override suspend fun getPokemon(idOrName: String): Pokemon {
        val response = try {
            api.getPokemon(idOrName)
        } catch (e: CancellationException) {
            // La corrutina se canceló: hay que relanzar la excepción para que la
            // cancelación se propague.
            throw e
        } catch (e: IOException) {
            // Sin conexión, timeout o fallo de DNS.
            throw PokemonException.Network(e)
        } catch (e: Exception) {
            // Cualquier otro fallo, por ejemplo un JSON que Gson no puede leer.
            throw PokemonException.Unknown(e)
        }

        // 404: el nombre o número buscado no existe. Se distingue del resto de
        // errores para mostrar un mensaje propio.
        if (response.code() == HTTP_NOT_FOUND) throw PokemonException.NotFound()

        // El servidor respondió con otro error (500…) o sin cuerpo.
        return response.body()?.takeIf { response.isSuccessful }
            ?: throw PokemonException.Server(response.code())
    }
}
