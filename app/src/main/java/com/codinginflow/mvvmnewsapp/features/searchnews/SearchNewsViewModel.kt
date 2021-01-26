package com.codinginflow.mvvmnewsapp.features.searchnews

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.codinginflow.mvvmnewsapp.data.NewsArticle
import com.codinginflow.mvvmnewsapp.data.NewsRepository
import kotlinx.coroutines.launch

class SearchNewsViewModel @ViewModelInject constructor(
    private val repository: NewsRepository,
    @Assisted state: SavedStateHandle
) : ViewModel() {

    private val currentQuery = state.getLiveData<String?>("currentQuery", null)
    val newsArticles = currentQuery.switchMap { query ->
        query?.let {
            repository.getSearchResultsPaged(query).asLiveData().cachedIn(viewModelScope)
        } ?: MutableLiveData(PagingData.empty())
    }

    val hasCurrentQuery = currentQuery.map { query ->
        query != null
    }

    var refreshInProgress = false

    var pendingScrollToTopAfterRefresh = false

    fun searchArticles(query: String) {
        currentQuery.value = query
    }

    fun onBookmarkClick(article: NewsArticle) {
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.update(updatedArticle)
        }
    }
}