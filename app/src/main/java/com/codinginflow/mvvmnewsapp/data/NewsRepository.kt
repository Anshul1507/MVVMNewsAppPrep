package com.codinginflow.mvvmnewsapp.data

import com.codinginflow.mvvmnewsapp.api.NewsApi
import com.codinginflow.mvvmnewsapp.util.Resource
import com.codinginflow.mvvmnewsapp.util.networkBoundResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val newsArticleDao: NewsArticleDao
) {

    fun getBreakingNews(): Flow<Resource<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                newsArticleDao.getAllArticles()
            },
            fetch = {
                val response = newsApi.getTopHeadlines()
                response.articles
            },
            saveFetchResult = { articles ->
                newsArticleDao.deleteAll()
                newsArticleDao.insert(articles)
            }
        )

    suspend fun searchNews(query: String): Resource<List<NewsArticle>> =
        try {
            delay(500)
            val response = newsApi.findNews(query)
            Resource.Success(response.articles)
        } catch (t: Throwable) {
            Resource.Error(t.localizedMessage ?: "An unknown error occurred")
        }

    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>> =
        newsArticleDao.getAllBookmarkedArticles()

    suspend fun update(article: NewsArticle) {
        newsArticleDao.update(article)
    }
}