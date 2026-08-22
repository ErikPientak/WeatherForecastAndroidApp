package com.example.weatherforecastandroidapp.data.remote

import com.example.weatherforecastandroidapp.data.remote.dto.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {

    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int,
        @Query("language") language: String,
        @Query("format") format: String,
    ): GeocodingResponse
}
