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
import java.io.InvalidObjectException

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
        val page = when (loadType) {
            LoadType.REFRESH -> {
                // TODO: 17.01.2021 Looks like this is pointless since REFRESH clears the search
                //  results from the database -> Probably just pass STARTING_PAGE_INDEX instead
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
            val serverSearchResults = apiResponse.articles
            val endOfPaginationReached = serverSearchResults.isEmpty()

            val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()
            val cachedBreakingNewsArticles = newsArticleDao.getCachedBreakingNews().first()

            val searchResults = serverSearchResults.map { serverSearchResultArticle ->
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
                    isBreakingNews = inBreakingNewsCache,
                    isBookmarked = bookmarked,
                    isSearchResult = true
                )
            }

            newsDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    Timber.d("Mediator REFRESH -> clearing old data")
                    newsDb.searchRemoteKeyDao().clearRemoteKeys()
                    newsArticleDao.resetSearchResults()
                    newsArticleDao.deleteAllObsoleteArticles()
                }

                val prevKey = if (page == NEWS_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val remoteKeys = serverSearchResults.map { article ->
                    SearchRemoteKeys(article.url, prevKey, nextKey)
                }
                newsDb.searchRemoteKeyDao().insertAll(remoteKeys)
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
    ): SearchRemoteKeys? =
        state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.url?.let { articleUrl ->
                newsDb.searchRemoteKeyDao().getRemoteKeyFromArticleUrl(articleUrl)
            }
        }
}