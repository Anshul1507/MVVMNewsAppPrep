package com.codinginflow.mvvmnewsapp.data

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.codinginflow.mvvmnewsapp.api.NewsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

private const val NEWS_STARTING_PAGE_INDEX = 1

class SearchNewsRemoteMediator(
    private val searchQuery: String,
    private val newsDb: NewsArticleDatabase,
    private val newsApi: NewsApi
) : RemoteMediator<Int, NewsArticle>() {

    private val newsArticleDao = newsDb.newsArticleDao()
    private val searchQueryDao = newsDb.searchQueryDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticle>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> NEWS_STARTING_PAGE_INDEX
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val nextKey = searchQueryDao.getLastCachedSearchResult(searchQuery)?.nextPageKey
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                nextKey
            }
        }

        return try {
            delay(1000)
            val apiResponse = newsApi.searchNews(searchQuery, page, state.config.pageSize)
            val serverSearchResults = apiResponse.articles
            val endOfPaginationReached = serverSearchResults.isEmpty()

            val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()
            val cachedBreakingNewsArticles = newsArticleDao.getCachedBreakingNews().first()

            val searchResultArticles = serverSearchResults.map { serverSearchResultArticle ->
                val bookmarked = bookmarkedArticles.any { bookmarkedArticle ->
                    bookmarkedArticle.url == serverSearchResultArticle.url
                }
                val inBreakingNewsCache =
                    cachedBreakingNewsArticles.any { breakingNewsArticle ->
                        breakingNewsArticle.url == serverSearchResultArticle.url
                    }
                NewsArticle(
                    title = serverSearchResultArticle.title,
                    url = serverSearchResultArticle.url,
                    urlToImage = serverSearchResultArticle.urlToImage,
                    publishedAt = serverSearchResultArticle.publishedAt,
                    isBreakingNews = inBreakingNewsCache,
                    isBookmarked = bookmarked,
                )
            }

            newsDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    searchQueryDao.clearSearchResultsForQuery(searchQuery)
                }

                val lastResultPosition =
                    searchQueryDao.getLastCachedSearchResult(searchQuery)?.queryPosition ?: 0
                var position = lastResultPosition + 1

                val prevKey = if (page == NEWS_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val searchResults = serverSearchResults.map { article ->
                    SearchResult(searchQuery, article.url, prevKey, nextKey, position++)
                }
                newsDb.newsArticleDao().insertAll(searchResultArticles)
                searchQueryDao.insertAll(searchResults)
            }
            MediatorResult.Success(endOfPaginationReached)
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            MediatorResult.Error(exception)
        }
    }
}