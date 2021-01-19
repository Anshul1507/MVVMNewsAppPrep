package com.codinginflow.mvvmnewsapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "news_articles")
data class NewsArticle(
    val title: String,
    @PrimaryKey val url: String,
    val urlToImage: String?,
    val publishedAt: Date,
    val isBookmarked: Boolean,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "search_results", primaryKeys = ["searchQuery", "articleUrl"])
// Foreign key on articleUrl deletes articles out of previous search queries on REPLACE
data class SearchResult(
    val searchQuery: String,
    val articleUrl: String,
    val prevPageKey: Int?,
    val nextPageKey: Int?,
    val queryPosition: Int
)

@Entity(tableName = "breaking_news")
data class BreakingNews(
    @PrimaryKey val articleUrl: String
)