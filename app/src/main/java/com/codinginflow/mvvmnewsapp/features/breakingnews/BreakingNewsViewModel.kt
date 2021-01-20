package com.codinginflow.mvvmnewsapp.features.breakingnews

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmnewsapp.data.NewsArticle
import com.codinginflow.mvvmnewsapp.data.NewsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class BreakingNewsViewModel @ViewModelInject constructor(
    private val repository: NewsRepository
) : ViewModel() {
    private val refreshTrigger = MutableSharedFlow<Refresh.NORMAL>(1).apply {
        tryEmit(Refresh.NORMAL)
    }

    private val forceRefresh = MutableSharedFlow<Refresh.FORCE>()

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val breakingNews = merge(refreshTrigger, forceRefresh).flatMapLatest { refresh ->
        Timber.d("forceRefresh = ${Refresh.FORCE == refresh}")
        repository.getBreakingNews(
            Refresh.FORCE == refresh, // this direction makes it Java null-safe
            onFetchFailed = { t ->
                showErrorMessage(t)
            }
        )
    }.asLiveData()

    fun onManualRefresh() {
        Timber.d("onManualRefresh()")
        viewModelScope.launch {
            forceRefresh.emit(Refresh.FORCE)
        }
    }

    fun onBookmarkClick(article: NewsArticle) {
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.update(updatedArticle)
        }
    }

    private fun showErrorMessage(t: Throwable) {
        viewModelScope.launch {
            eventChannel.send(Event.ShowErrorMessage(t))
        }
    }

    sealed class Refresh {
        object FORCE: Refresh()
        object NORMAL: Refresh()
    }

    sealed class Event {
        data class ShowErrorMessage(val throwable: Throwable) : Event()
    }
}