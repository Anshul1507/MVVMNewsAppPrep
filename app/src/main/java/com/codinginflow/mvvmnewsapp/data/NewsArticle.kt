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
    val isBreakingNews: Boolean,
    val isBookmarked: Boolean,
    val updatedAt: Long = System.currentTimeMillis(),
)