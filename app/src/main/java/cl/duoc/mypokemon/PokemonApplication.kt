package cl.duoc.mypokemon

import android.app.Application
import cl.duoc.mypokemon.data.AppContainer
import cl.duoc.mypokemon.data.DefaultAppContainer

/**
 * `Application` propia de la app (declarada en el manifest con `android:name`).
 *
 * Crea el `AppContainer` una sola vez. Las factories de los ViewModels lo leen para
 * obtener sus dependencias.
 */
class PokemonApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
