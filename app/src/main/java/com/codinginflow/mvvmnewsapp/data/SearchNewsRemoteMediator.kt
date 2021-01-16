package com.codinginflow.mvvmnewsapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.codinginflow.mvvmnewsapp.api.NewsApi
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException

private const val NEWS_STARTING_PAGE_INDEX = 1

class SearchNewsRemoteMediator(
    private val searchQuery: String,
    private val newsDb: NewsArticleDatabase,
    private val newsApi: NewsApi
) : RemoteMediator<Int, NewsArticle>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticle>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: NEWS_STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                    ?: throw InvalidObjectException("Remote key should not be null for $loadType")
                val prevKey = remoteKeys.prevKey
                if (prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                remoteKeys.prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                if (remoteKeys == null || remoteKeys.nextKey == null) {
                    throw InvalidObjectException("Remote key should not be null for $loadType")
                }
                remoteKeys.nextKey
            }
        }

        return try {
            delay(2000)
            val apiResponse = newsApi.searchNews(searchQuery, page, state.config.pageSize)
            val articles = apiResponse.articles
            val endOfPaginationReached = articles.isEmpty()

            newsDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    newsDb.searchRemoteKeyDao().clearRemoteKeys()
                    newsDb.newsArticleDao().resetSearchResults()
                    newsDb.newsArticleDao().deleteAllObsoleteArticles()
                }
                val prevKey = if (page == NEWS_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val remoteKeys = articles.map { article ->
                    SearchRemoteKeys(article.url, prevKey, nextKey)
                }
                newsDb.searchRemoteKeyDao().insertAll(remoteKeys)
                // TODO: 16.01.2021 Implement logic to maintain bookmarks and breaking news
                val searchResults = articles.map { article ->
                    article.copy(isSearchResult = true)
                }
                newsDb.newsArticleDao().insertAll(searchResults)
            }
            MediatorResult.Success(endOfPaginationReached)
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, NewsArticle>): SearchRemoteKeys? =
        state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { article ->
                newsDb.searchRemoteKeyDao().getRemoteKeyFromArticleUrl(article.url)
            }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, NewsArticle>): SearchRemoteKeys? =
        state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { article ->
                newsDb.searchRemoteKeyDao().getRemoteKeyFromArticleUrl(article.url)
            }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, NewsArticle>
    ) : SearchRemoteKeys? =
        state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.url?.let { articleUrl ->
                newsDb.searchRemoteKeyDao().getRemoteKeyFromArticleUrl(articleUrl)
            }
        }
}