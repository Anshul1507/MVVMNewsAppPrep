package com.codinginflow.mvvmnewsapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM news_articles")
    fun getAllArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1")
    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: NewsArticle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(articles: List<NewsArticle>)

    @Update
    suspend fun update(article: NewsArticle)

    @Query("DELETE FROM news_articles")
    suspend fun deleteAll()
}