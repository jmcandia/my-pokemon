package com.example.mypokemon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Regla de JUnit que reemplaza `Dispatchers.Main` por un dispatcher de test.
 *
 * `viewModelScope` usa `Dispatchers.Main`, que en tests unitarios (JVM, sin Android)
 * no existe. Con `StandardTestDispatcher` las corrutinas no se ejecutan hasta llamar
 * a `advanceUntilIdle()`, lo que permite ver el estado `Loading` intermedio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
