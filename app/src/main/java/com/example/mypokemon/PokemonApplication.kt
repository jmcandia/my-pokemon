package com.example.mypokemon

import android.app.Application
import com.example.mypokemon.data.AppContainer
import com.example.mypokemon.data.DefaultAppContainer

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
