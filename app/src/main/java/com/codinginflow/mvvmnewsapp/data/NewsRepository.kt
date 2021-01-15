package com.codinginflow.mvvmnewsapp.data

import com.codinginflow.mvvmnewsapp.api.NewsApi
import com.codinginflow.mvvmnewsapp.util.Resource
import com.codinginflow.mvvmnewsapp.util.networkBoundResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val newsArticleDao: NewsArticleDao
) {

    fun getBreakingNews(onFetchFailed: (Throwable) -> Unit): Flow<Resource<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                newsArticleDao.getTopHeadlines()
            },
            fetch = {
                val response = newsApi.getTopHeadlines()
                response.articles
            },
            saveFetchResult = { serverBreakingNewsArticles ->
                // TODO: 14.01.2021 transaction
                val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()
                val breakingNewsArticles = serverBreakingNewsArticles.map { serverBreakingNewsArticle ->
                    val bookmarked = bookmarkedArticles.any { bookmarkedArticle ->
                        bookmarkedArticle.url == serverBreakingNewsArticle.url
                    }
                    serverBreakingNewsArticle.copy(isBreakingNews = true, isBookmarked = bookmarked)
                }

                newsArticleDao.resetAllBreakingNews()
                newsArticleDao.insert(breakingNewsArticles)
                newsArticleDao.deleteAllObsoleteArticles()
            },
            shouldFetch = {
                // TODO: 14.01.2021 Implement timestamp based approach
                true
            },
            // TODO: 15.01.2021 Is this legit for 1 time error messages?
            onFetchFailed = onFetchFailed
        )

    suspend fun searchNews(query: String): Resource<List<NewsArticle>> =
        try {
            delay(500)
            val response = newsApi.findNews(query)
            Resource.Success(response.articles)
        } catch (t: Throwable) {
            Resource.Error(t)
        }

    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>> =
        newsArticleDao.getAllBookmarkedArticles()

    suspend fun update(article: NewsArticle) {
        newsArticleDao.update(article)
    }

    suspend fun deleteAllBookmarks() {
        newsArticleDao.deleteAllBookmarks()
    }
}