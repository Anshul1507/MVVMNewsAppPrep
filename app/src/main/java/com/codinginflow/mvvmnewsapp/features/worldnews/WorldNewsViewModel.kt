package com.codinginflow.mvvmnewsapp.features.worldnews

import androidx.lifecycle.*
import com.codinginflow.mvvmnewsapp.data.NewsArticle
import com.codinginflow.mvvmnewsapp.data.NewsRepository
import com.codinginflow.mvvmnewsapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WorldNewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val refreshTrigger = object : MutableLiveData<Refresh>() {
        override fun onActive() {
            if (breakingNews.value !is Resource.Loading) {
                value = Refresh.NORMAL
            }
        }
    }

    private val eventFlow = MutableSharedFlow<Event>()
    val events: Flow<Event> = eventFlow

    val breakingNews: LiveData<Resource<List<NewsArticle>>> = refreshTrigger.switchMap { refresh ->
        Timber.d("forceRefresh = ${Refresh.FORCE == refresh}")
        repository.getBreakingNews(
            Refresh.FORCE == refresh, // this direction makes it Java null-safe
            onFetchSuccess = {
                viewModelScope.launch {
                    eventFlow.emit(Event.ScrollToTop)
                }
            },
            onFetchFailed = { t ->
                viewModelScope.launch {
                    eventFlow.emit(Event.ShowErrorMessage(t))
                }
            }
        ).asLiveData()
    }

    init {
        viewModelScope.launch {
            // TODO: 26.01.2021 Test this a bit more to make sure it doesn't break anything
            repository.deleteArticlesFromCacheOlderThan(
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(
                    30
                )
            )
        }
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

    enum class Refresh {
        FORCE, NORMAL
    }

    sealed class Event {
        object ScrollToTop : Event()
        data class ShowErrorMessage(val error: Throwable) : Event()
    }
}