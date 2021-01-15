package com.codinginflow.mvvmnewsapp.api

import com.codinginflow.mvvmnewsapp.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NewsApi {

    companion object {
        const val BASE_URL = "https://newsapi.org/v2/"
        const val API_KEY = BuildConfig.NEWS_API_ACCESS_KEY
    }

    @Headers("X-Api-Key: $API_KEY")
    @GET("everything")
    suspend fun findNews(
        @Query("q") query: String
    ): NewsResponse

    @Headers("X-Api-Key: $API_KEY")
    // TODO: 15.01.2021 Small page size for testing purposes
    @GET("top-headlines?country=us&pageSize=5")
    suspend fun getTopHeadlines() : NewsResponse
}