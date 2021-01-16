package com.codinginflow.mvvmnewsapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "news_articles")
data class NewsArticle(
    val title: String,
    @PrimaryKey val url: String,
    val urlToImage: String?,
    val isBreakingNews: Boolean,
    val isBookmarked: Boolean,
    val isSearchResult: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)