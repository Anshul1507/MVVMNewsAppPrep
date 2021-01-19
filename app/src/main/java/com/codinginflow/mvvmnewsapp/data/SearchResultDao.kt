package com.codinginflow.mvvmnewsapp.data

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface SearchResultDao {

    @Query("SELECT * FROM search_results INNER JOIN news_articles ON articleUrl = url WHERE searchQuery = :query ORDER BY queryPosition")
    fun getSearchResultsPaged(query: String): PagingSource<Int, NewsArticle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchResult: SearchResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(searchResults: List<SearchResult>)

    @Update
    suspend fun update(searchResult: SearchResult)

    @Query("SELECT * FROM search_results WHERE searchQuery = :query AND articleUrl = :articleUrl")
    suspend fun getSearchResult(query: String, articleUrl: String): SearchResult

    @Query("SELECT * FROM search_results WHERE searchQuery = :query ORDER BY queryPosition DESC LIMIT 1")
    suspend fun getLastCachedSearchResult(query: String): SearchResult?

    @Query("DELETE FROM search_results WHERE searchQuery = :query")
    suspend fun clearSearchResultsForQuery(query: String)
}