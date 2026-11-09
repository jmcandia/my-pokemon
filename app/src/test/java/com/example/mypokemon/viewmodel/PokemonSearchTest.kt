package com.example.mypokemon.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonSearchTest {

    @Test
    fun `pasa el nombre a minúsculas y quita espacios de los extremos`() {
        assertEquals("pikachu", normalizeQuery("  Pikachu "))
    }

    @Test
    fun `quita los ceros a la izquierda de un número`() {
        // PokéAPI responde 404 a "025".
        assertEquals("25", normalizeQuery("025"))
        assertEquals("0", normalizeQuery("000"))
    }

    @Test
    fun `convierte nombres con espacios, puntos, apóstrofos y tildes`() {
        assertEquals("mr-mime", normalizeQuery("Mr. Mime"))
        assertEquals("farfetchd", normalizeQuery("Farfetch'd"))
        assertEquals("flabebe", normalizeQuery("Flabébé"))
        assertEquals("tapu-koko", normalizeQuery("Tapu   Koko"))
    }

    @Test
    fun `devuelve vacío si no hay nada que buscar`() {
        assertEquals("", normalizeQuery("   "))
        assertEquals("", normalizeQuery("."))
    }
}
