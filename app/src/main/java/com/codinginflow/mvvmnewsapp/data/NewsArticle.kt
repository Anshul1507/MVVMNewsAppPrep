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
    val thumbnailUrl: String?,
    val isBookmarked: Boolean,
    val updatedAt: Long = System.currentTimeMillis(),
)

// url as a primary key gets rid of duplicates
@Entity(tableName = "search_results", primaryKeys = ["searchQuery", "articleUrl"])
// Foreign key on articleUrl deletes articles out of previous search queries on REPLACE
data class SearchResult(
    val searchQuery: String,
    val articleUrl: String,
    val nextPageKey: Int?,
    val queryPosition: Int
)

@Entity(tableName = "breaking_news")
data class BreakingNews(
    val articleUrl: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0 // we need this for the order, otherwise an article can move around if it also appears in a search query
)