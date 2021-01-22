package com.codinginflow.mvvmnewsapp.api

import com.codinginflow.mvvmnewsapp.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi { // TODO: 22.01.2021 Does "Guardian" have to be in the class names?

    companion object {
        const val BASE_URL = "https://content.guardianapis.com/"
        const val API_KEY = BuildConfig.GUARDIAN_API_KEY
    }

    @GET("search?api-key=$API_KEY&show-fields=thumbnail")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("page-size") pageSize: Int,
    ): NewsResponse

    // TODO: 15.01.2021 Small page size for testing purposes
    @GET("search?api-key=$API_KEY&section=world&page-size=100&show-fields=thumbnail")
    suspend fun getWorldNews(): NewsResponse
}