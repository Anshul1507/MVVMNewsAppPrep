package com.codinginflow.mvvmnewsapp.features.searchnews

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.codinginflow.mvvmnewsapp.data.NewsArticle
import com.codinginflow.mvvmnewsapp.data.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchNewsViewModel @Inject constructor(
    private val repository: NewsRepository,
    state: SavedStateHandle
) : ViewModel() {

    // TODO: 30.01.2021 This shows the cached data after process death without the RecyclerView being invisible
    private val currentQuery = state.getLiveData<String?>("currentQuery")
    val newsArticles = currentQuery.switchMap { query ->
        newQueryInProgress = true
        repository.getSearchResultsPaged(query).asLiveData().cachedIn(viewModelScope)
    }

    var refreshInProgress = false

    var newQueryInProgress = false

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