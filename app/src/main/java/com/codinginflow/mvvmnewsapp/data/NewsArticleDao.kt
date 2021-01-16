package com.codinginflow.mvvmnewsapp.data

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM news_articles")
    fun getAllArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE isBreakingNews = 1")
    fun getAllBreakingNews(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1")
    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE isSearchResult = 1")
    fun getAllSearchResults(): PagingSource<Int, NewsArticle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<NewsArticle>)

    @Update
    suspend fun update(article: NewsArticle)

    @Update
    suspend fun update(articles: List<NewsArticle>)

    @Query("UPDATE news_articles SET isBookmarked = 0")
    suspend fun resetBookmarks()

    @Query("UPDATE news_articles SET isBreakingNews = 0")
    suspend fun resetBreakingNews()

    @Query("UPDATE news_articles SET isSearchResult = 0")
    suspend fun resetSearchResults()

    @Query("DELETE FROM news_articles WHERE isBookmarked = 0 AND isBreakingNews = 0 AND isSearchResult = 0")
    suspend fun deleteAllObsoleteArticles()
}