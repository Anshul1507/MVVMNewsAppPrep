package com.codinginflow.mvvmnewsapp.features.worldnews

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmnewsapp.data.NewsArticle
import com.codinginflow.mvvmnewsapp.data.NewsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class WorldNewsViewModel @ViewModelInject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val refreshTrigger = object : MutableLiveData<Refresh>() {
        override fun onActive() {
            Timber.d("Fragment onStart")
            value = Refresh.NORMAL
        }
    }

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    val breakingNews = refreshTrigger.switchMap { refresh ->
        Timber.d("forceRefresh = ${Refresh.FORCE == refresh}")
        repository.getBreakingNews(
            Refresh.FORCE == refresh, // this direction makes it Java null-safe
            onFetchFailed = { t ->
                showErrorMessage(t)
            }
        ).asLiveData()
    }

    fun onManualRefresh() {
        Timber.d("onManualRefresh()")
        refreshTrigger.value = Refresh.FORCE
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

    enum class Refresh {
        FORCE, NORMAL
    }

    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }
}