package com.codinginflow.mvvmnewsapp.api

import java.util.*

data class NewsArticleDto(
    val title: String,
    val url: String,
    val urlToImage: String?,
    val publishedAt: Date
)