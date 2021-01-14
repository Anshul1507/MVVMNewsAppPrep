package com.codinginflow.mvvmnewsapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_articles")
data class NewsArticle(
    val title: String,
    @PrimaryKey  val url: String,
    val urlToImage: String?,
    val isBookmarked: Boolean = false
)