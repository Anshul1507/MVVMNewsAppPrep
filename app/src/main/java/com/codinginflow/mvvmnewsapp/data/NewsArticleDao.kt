package com.codinginflow.mvvmnewsapp.data

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM news_articles WHERE isBreakingNews = 1")
    fun getCachedBreakingNews(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1")
    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<NewsArticle>) : LongArray

    @Update
    suspend fun update(article: NewsArticle)

    @Update
    suspend fun update(articles: List<NewsArticle>)

    @Query("UPDATE news_articles SET isBookmarked = 0")
    suspend fun resetBookmarks()

    @Query("UPDATE news_articles SET isBreakingNews = 0")
    suspend fun resetBreakingNews()
}