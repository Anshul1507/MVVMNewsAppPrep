package com.codinginflow.mvvmnewsapp.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.codinginflow.mvvmnewsapp.api.NewsApi
import com.codinginflow.mvvmnewsapp.util.Resource
import com.codinginflow.mvvmnewsapp.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.text.DateFormat
import javax.inject.Inject

const val FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000 // short span for easier testing

class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val newsDb: NewsArticleDatabase,
    private val newsArticleDatabase: NewsArticleDatabase
) {
    private val newsArticleDao = newsDb.newsArticleDao()

    fun getBreakingNews(forceRefresh: Boolean, onFetchFailed: (Throwable) -> Unit): Flow<Resource<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                newsArticleDao.getAllBreakingNewsArticles()
            },
            fetch = {
                val response = newsApi.getWorldNews()
                Timber.d("Fetched: ${response.response.results}")
                response.response.results
            },
            saveFetchResult = { serverBreakingNewsArticles ->
                val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()

                val breakingNewsArticles =
                    serverBreakingNewsArticles.map { serverBreakingNewsArticle ->
                        val bookmarked = bookmarkedArticles.any { bookmarkedArticle ->
                            bookmarkedArticle.url == serverBreakingNewsArticle.webUrl
                        }
                        NewsArticle(
                            title = serverBreakingNewsArticle.webTitle,
                            url = serverBreakingNewsArticle.webUrl,
                            thumbnail = serverBreakingNewsArticle.fields?.thumbnail,
                            isBookmarked = bookmarked,
                        )
                    }

                newsDb.withTransaction {
                    val breakingNews = breakingNewsArticles.map { article ->
                        BreakingNews(article.url)
                    }

                    newsArticleDao.deleteAllBreakingNews()
                    newsArticleDao.insertArticles(breakingNewsArticles)
                    newsArticleDao.insertBreakingNews(breakingNews)
                }
            },
            shouldFetch = { cachedArticles ->
                Timber.d("shouldFetch with forceRefresh = $forceRefresh")
                if (forceRefresh) {
                    true
                } else {
                    val sortedArticles = cachedArticles.sortedBy { article ->
                        article.updatedAt
                    }
                    val oldestTimestamp = sortedArticles.firstOrNull()?.updatedAt
                    val needsRefresh =
                        oldestTimestamp == null || oldestTimestamp < System.currentTimeMillis() - FIVE_MINUTES_IN_MILLIS
                    Timber.d(
                        "oldestTimestamp = ${
                            DateFormat.getDateTimeInstance().format(oldestTimestamp ?: 0)
                        }"
                    )
                    Timber.d(
                        "currentTimestamp = ${
                            DateFormat.getDateTimeInstance().format(System.currentTimeMillis())
                        }"
                    )
                    Timber.d("needsRefresh: $needsRefresh")
                    needsRefresh
                }
            },
            onFetchFailed = onFetchFailed
        )

    fun getSearchResults(query: String): Flow<PagingData<NewsArticle>> =
        Pager(
            config = PagingConfig(pageSize = 50, enablePlaceholders = false),
            remoteMediator = SearchNewsRemoteMediator(query, newsArticleDatabase, newsApi),
            pagingSourceFactory = { newsArticleDao.getSearchResultArticlesPaged(query) }
        ).flow

    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>> =
        newsArticleDao.getAllBookmarkedArticles()

    suspend fun update(article: NewsArticle) {
        newsArticleDao.update(article)
    }

    suspend fun deleteAllBookmarks() {
        newsArticleDao.deleteAllBookmarks()
    }
}