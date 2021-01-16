package com.codinginflow.mvvmnewsapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.codinginflow.mvvmnewsapp.api.NewsApi
import com.codinginflow.mvvmnewsapp.util.Resource
import com.codinginflow.mvvmnewsapp.util.networkBoundResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val newsDb: NewsArticleDatabase,
    private val newsArticleDatabase: NewsArticleDatabase
) {
    private val newsArticleDao = newsDb.newsArticleDao()

    fun getBreakingNews(onFetchFailed: (Throwable) -> Unit): Flow<Resource<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                newsArticleDao.getAllBreakingNews()
            },
            fetch = {
                val response = newsApi.getTopHeadlines()
                response.articles
            },
            saveFetchResult = { serverBreakingNewsArticles ->
                val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()
                val breakingNewsArticles =
                    serverBreakingNewsArticles.map { serverBreakingNewsArticle ->
                        val bookmarked = bookmarkedArticles.any { bookmarkedArticle ->
                            bookmarkedArticle.url == serverBreakingNewsArticle.url
                        }
                        serverBreakingNewsArticle.copy(
                            isBreakingNews = true,
                            isBookmarked = bookmarked
                        )
                    }

                newsDb.withTransaction {
                    newsArticleDao.resetBreakingNews()
                    newsArticleDao.insertAll(breakingNewsArticles)
                    newsArticleDao.deleteAllObsoleteArticles()
                }
            },
            shouldFetch = {
                // TODO: 14.01.2021 Implement timestamp based approach
                true
            },
            onFetchFailed = onFetchFailed
        )

    fun getSearchResults(query: String): Flow<PagingData<NewsArticle>> =
        Pager(
            config = PagingConfig(pageSize = 20, maxSize = 100, enablePlaceholders = false),
            remoteMediator = SearchNewsRemoteMediator(query, newsArticleDatabase, newsApi),
            pagingSourceFactory = { newsArticleDatabase.newsArticleDao().getAllSearchResults() }
        ).flow

    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>> =
        newsArticleDao.getAllBookmarkedArticles()

    suspend fun update(article: NewsArticle) {
        newsArticleDao.update(article)
    }

    suspend fun deleteAllBookmarks() {
        newsArticleDao.resetBookmarks()
    }
}