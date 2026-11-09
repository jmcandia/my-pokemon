package cl.duoc.mypokemon.viewmodel

import java.text.Normalizer

/**
 * Convierte lo que escribe el usuario en el identificador que espera PokéAPI.
 *
 * - Número: sin ceros a la izquierda (`"025"` → `"25"`; PokéAPI da 404 con `"025"`).
 * - Nombre: en minúsculas, sin tildes, sin puntos ni apóstrofos y con guiones en vez
 *   de espacios. Así `"Mr. Mime"` → `"mr-mime"`, `"Flabébé"` → `"flabebe"` y
 *   `"Farfetch'd"` → `"farfetchd"`, que son los nombres de PokéAPI.
 *
 * Devuelve `""` si no queda nada que buscar.
 */
internal fun normalizeQuery(query: String): String {
    val trimmed = query.trim()
    if (trimmed.isNotEmpty() && trimmed.all { it.isDigit() }) {
        return trimmed.trimStart('0').ifEmpty { "0" }
    }
    // NFD separa cada letra de su tilde ("é" → "e" + "´"); luego se borran las tildes.
    val withoutAccents = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
    return withoutAccents
        .lowercase()
        .replace(Regex("[.'’]"), "")
        .trim()
        .replace(Regex("\\s+"), "-")
}
