package com.codinginflow.mvvmnewsapp.data

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.codinginflow.mvvmnewsapp.api.NewsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

private const val NEWS_STARTING_PAGE_INDEX = 1

class SearchNewsRemoteMediator(
    private val searchQuery: String,
    private val newsDb: NewsArticleDatabase,
    private val newsApi: NewsApi
) : RemoteMediator<Int, NewsArticle>() {

    private val newsArticleDao = newsDb.newsArticleDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticle>
    ): MediatorResult {
        Timber.d("load with anchorPosition = ${state.anchorPosition}")
        val page = when (loadType) {
            LoadType.REFRESH -> {
                Timber.d("Start REFRESH")
                val nextPageKey = getNextPageKeyClosestToCurrentPosition(state)
                Timber.d("return REFRESH with nextKey = $nextPageKey")
                nextPageKey?.minus(1) ?: NEWS_STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                Timber.d("Start PREPEND")
                val prevPageKey = getPreviousPageKeyForFirstItem(state)
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                Timber.d("return PREPEND with prevPageKey = $prevPageKey")
                prevPageKey
            }
            LoadType.APPEND -> {
                Timber.d("Start APPEND")
                val nextPageKey = getNextPageKeyForLastItem(state)
                // TODO: 21.01.2021 The previousPage key should never be null but this should be fine (test with "asdasd")
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                Timber.d("return APPEND with nextPageKey = $nextPageKey")
                nextPageKey
            }
        }

        return try {
            Timber.d("start of try-block")
            delay(1000)
            val loadSize = when (loadType) {
                LoadType.REFRESH -> state.config.initialLoadSize
                else -> state.config.initialLoadSize
            }
            val apiResponse = newsApi.searchNews(searchQuery, page, loadSize)
            val serverSearchResults = apiResponse.response.results
            Timber.d("articles fetched = ${serverSearchResults.size}")
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

                // TODO: 21.01.2021 This will not work for prepend
                val lastResultPosition = getQueryPositionForLastItem(state) ?: 0
                var position = lastResultPosition + 1

                val prevKey = if (page == NEWS_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val searchResults = searchResultArticles.map { article ->
                    SearchResult(searchQuery, article.url, prevKey, nextKey, position++)
                }
                newsArticleDao.insertArticles(searchResultArticles)
                newsArticleDao.insertSearchResults(searchResults)
                // TODO: 21.01.2021 Delete old outdated articles?
            }
            MediatorResult.Success(endOfPaginationReached)
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getNextPageKeyForLastItem(state: PagingState<Int, NewsArticle>): Int? {
        return state.lastItemOrNull()?.let { article ->
            newsArticleDao.getSearchResult(article.url).nextPageKey
        }
    }

    private suspend fun getPreviousPageKeyForFirstItem(state: PagingState<Int, NewsArticle>): Int? {
        return state.firstItemOrNull()?.let { article ->
            newsArticleDao.getSearchResult(article.url).prevPageKey
        }
    }

    private suspend fun getNextPageKeyClosestToCurrentPosition(state: PagingState<Int, NewsArticle>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.url?.let { articleUrl ->
                newsArticleDao.getSearchResult(articleUrl).nextPageKey
            }
        }
    }

    private suspend fun getQueryPositionForLastItem(state: PagingState<Int, NewsArticle>): Int? {
        return state.lastItemOrNull()?.let { article ->
            newsArticleDao.getSearchResult(article.url).queryPosition
        }
    }
}