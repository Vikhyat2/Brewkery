package com.bookverse.brewkery.data.remote


import retrofit2.http.GET
import retrofit2.http.Path

interface BrewkeryApi {

    /** Full menu + store config. */
    @GET("data.json")
    suspend fun getMenu(): MenuResponse

    /** Single item resource, ids 1..6. */
    @GET("api/items/{id}.json")
    suspend fun getItem(@Path("id") id: Int): MenuItem

    companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/VivekShah138/Brewkery/main/"
    }
}
