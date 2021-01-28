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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val newsArticleDb   : NewsArticleDatabase,
) {
    private val newsArticleDao = newsArticleDb.newsArticleDao()

    fun getBreakingNews(
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                newsArticleDao.getAllBreakingNewsArticles()
            },
            fetch = {
                val response = newsApi.getWorldNews()
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

                newsArticleDb.withTransaction {
                    val breakingNews = breakingNewsArticles.map { article ->
                        BreakingNews(article.url)
                    }

                    newsArticleDao.deleteAllBreakingNews()
                    newsArticleDao.insertArticles(breakingNewsArticles)
                    newsArticleDao.insertBreakingNews(breakingNews)
                }
            },
            shouldFetch = { cachedArticles ->
                if (forceRefresh) {
                    true
                } else {
                    val sortedArticles = cachedArticles.sortedBy { article ->
                        article.updatedAt
                    }
                    val oldestTimestamp = sortedArticles.firstOrNull()?.updatedAt
                    val needsRefresh =
                        oldestTimestamp == null || oldestTimestamp < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(
                            5
                        )
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
            onFetchSuccess = onFetchSuccess,
            onFetchFailed = onFetchFailed
        )

    fun getSearchResultsPaged(query: String): Flow<PagingData<NewsArticle>> =
        Pager(
            // enabledPlaceholders true (default) makes scrollToPosition(0) work after dropping pages
            // normally we should be able to set a maxSize without PREPEND but there is currently a bug that causes an IndexOutOfBoundsException
            // TODO: 26.01.2021 Update dependency when next paging release is out
            config = PagingConfig(pageSize = 50),
            remoteMediator = SearchNewsRemoteMediator(query, newsArticleDb, newsApi),
            pagingSourceFactory = { newsArticleDao.getSearchResultArticlesPaged(query) }
        ).flow

    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>> =
        newsArticleDao.getAllBookmarkedArticles()

    suspend fun update(article: NewsArticle) {
        newsArticleDao.update(article)
    }

    suspend fun resetAllBookmarks() {
        newsArticleDao.resetAllBookmarks()
    }

    suspend fun deleteArticlesFromCacheOlderThan(timeStampInMillis: Long) {
        newsArticleDao.deleteArticlesFromCacheOlderThan(timeStampInMillis)
    }
}