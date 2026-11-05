package com.example.mypokemon.data.repository

import com.example.mypokemon.data.remote.RetrofitInstance
import com.example.mypokemon.model.Pokemon

class PokemonRepository {

    suspend fun getPokemon(idOrName: String): Pokemon? {
        val response = RetrofitInstance.api.getPokemon(idOrName)
        return if (response.isSuccessful && response.body() != null) {
            response.body()
        } else {
            null
        }
    }
}