package com.codinginflow.mvvmnewsapp.data

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface SearchArticleCrossRefDao {

    @Transaction
    @Query("SELECT * FROM article_search_cross_refs LEFT OUTER JOIN news_articles ON articleUrl = url WHERE searchQuery = :query")
    fun getSearchResultsForQuery(query: String): PagingSource<Int, NewsArticle>

    @Query("SELECT * FROM article_search_cross_refs WHERE articleUrl = :articleUrl AND searchQuery = :searchQuery")
    suspend fun getCrossRefs(articleUrl: String, searchQuery: String): SearchArticleCrossRef

    @Query("DELETE FROM article_search_cross_refs WHERE searchQuery = :searchQuery")
    suspend fun deleteCrossRefsForQuery(searchQuery: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchArticleCrossRefs: List<SearchArticleCrossRef>)

    @Query("UPDATE article_search_cross_refs SET searchQuery = NULL WHERE searchQuery = :searchQuery")
    suspend fun resetSearchResults(searchQuery: String)
}