package com.example.mypokemon.data.repository

import com.example.mypokemon.data.remote.ApiService
import com.example.mypokemon.model.Pokemon

class PokemonRepository(
    private val api: ApiService
) {

    suspend fun getPokemon(idOrName: String): Pokemon? {
        val response = api.getPokemon(idOrName)
        return if (response.isSuccessful && response.body() != null) {
            response.body()
        } else {
            null
        }
    }
}
