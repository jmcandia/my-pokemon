package com.example.mypokemon.ui.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Texto que el ViewModel entrega a la UI sin resolverlo.
 *
 * El ViewModel no lee `strings.xml` (no tiene `Context`): indica qué recurso usar y
 * con qué argumentos, y la UI lo resuelve con `asString()`. Así los textos se pueden
 * traducir sin tocar el ViewModel, y los tests comparan IDs de recurso.
 */
sealed interface UiText {
    // Texto que no se traduce, por ejemplo el nombre de un Pokémon.
    data class Plain(val value: String) : UiText

    // Recurso de `strings.xml` con sus argumentos de formato (`%1$d`, `%2$s`…).
    data class Resource(
        @StringRes val id: Int,
        val args: List<Any> = emptyList()
    ) : UiText
}

/** Resuelve el texto con los recursos del idioma actual. */
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Plain -> value
    is UiText.Resource -> stringResource(id, *args.toTypedArray())
}
