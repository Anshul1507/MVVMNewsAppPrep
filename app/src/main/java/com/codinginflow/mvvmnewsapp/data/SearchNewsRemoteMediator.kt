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
    private val newsApi: NewsApi,
) : RemoteMediator<Int, NewsArticle>() {

    private val newsArticleDao = newsDb.newsArticleDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticle>
    ): MediatorResult {
        try {
            val page = when (loadType) {
                LoadType.REFRESH -> NEWS_STARTING_PAGE_INDEX
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val nextKey = getNextPageKeyForLastItem(state)
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    nextKey
                }
            }

            val apiResponse = newsApi.searchNews(searchQuery, page, state.config.pageSize)
//            delay(1000) // for testing purposes
            val serverSearchResults = apiResponse.response.results
            val endOfPaginationReached = serverSearchResults.isEmpty()

            val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()

            val searchResultArticles = serverSearchResults.map { serverSearchResultArticle ->
                val bookmarked = bookmarkedArticles.any { bookmarkedArticle ->
                    bookmarkedArticle.url == serverSearchResultArticle.webUrl
                }

                NewsArticle(
                    title = serverSearchResultArticle.webTitle,
                    url = serverSearchResultArticle.webUrl,
                    thumbnail = serverSearchResultArticle.fields?.thumbnail,
                    isBookmarked = bookmarked,
                )
            }

            newsDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    newsArticleDao.clearSearchResultsForQuery(searchQuery)
                }

                // TODO: 26.01.2021 can we get race conditions with other loads here?
                val lastQueryPosition = newsArticleDao.getLastQueryPosition(searchQuery) ?: 0
                var queryPosition = lastQueryPosition + 1

                val nextPageKey = page + 1 // TODO: 26.01.2021 I think I can ignore endOfPaginationReached here

                val searchResults = searchResultArticles.map { article ->
                    SearchResult(searchQuery, article.url, nextPageKey, queryPosition++)
                }
                newsArticleDao.insertArticles(searchResultArticles)
                newsArticleDao.insertSearchResults(searchResults)
            }
            return MediatorResult.Success(endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getNextPageKeyForLastItem(state: PagingState<Int, NewsArticle>): Int? {
        return state.lastItemOrNull()?.let { article ->
            newsArticleDao.getSearchResult(article.url).nextPageKey
        }
    }
}