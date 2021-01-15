package com.codinginflow.mvvmnewsapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO: 15.01.2021 Separate classes for API response and DB object?

@Entity(tableName = "news_articles")
data class NewsArticle(
    val title: String,
    @PrimaryKey val url: String,
    val urlToImage: String?,
    val isBreakingNews: Boolean,
    val isBookmarked: Boolean = false,
)