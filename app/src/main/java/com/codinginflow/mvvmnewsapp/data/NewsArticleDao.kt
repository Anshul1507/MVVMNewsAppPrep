package com.codinginflow.mvvmnewsapp.data

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM breaking_news INNER JOIN news_articles ON articleUrl = url")
    fun getAllBreakingNews(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM search_results INNER JOIN news_articles ON articleUrl = url WHERE searchQuery = :query ORDER BY queryPosition")
    fun getSearchResultsPaged(query: String): PagingSource<Int, NewsArticle>

    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1")
    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM search_results WHERE searchQuery = :query ORDER BY queryPosition DESC LIMIT 1")
    suspend fun getLastCachedSearchResult(query: String): SearchResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>) : LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreakingNews(breakingNews: List<BreakingNews>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchResults: List<SearchResult>)

    @Update
    suspend fun update(article: NewsArticle)

    @Update
    suspend fun update(articles: List<NewsArticle>)

    @Query("UPDATE news_articles SET isBookmarked = 0")
    suspend fun resetBookmarks()

    @Query("DELETE FROM search_results WHERE searchQuery = :query")
    suspend fun clearSearchResultsForQuery(query: String)

    @Query("DELETE FROM breaking_news")
    suspend fun deleteAllBreakingNews()
}