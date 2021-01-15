package com.codinginflow.mvvmnewsapp.breakingnews

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmnewsapp.data.NewsArticle
import com.codinginflow.mvvmnewsapp.data.NewsRepository
import kotlinx.coroutines.launch

class BreakingNewsViewModel @ViewModelInject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val refreshTrigger = MutableLiveData(Unit)

    val breakingNews = refreshTrigger.switchMap {
        repository.getBreakingNews(
            onFetchFailed = {
                // TODO: 15.01.2021 Show one-off error message
            }
        ).asLiveData()
    }

    fun onRefresh() {
        refreshTrigger.value = Unit
    }

    fun onBookmarkClick(article: NewsArticle) {
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.update(updatedArticle)
        }
    }

    /*  private val _newsArticles = MutableLiveData<Resource<List<NewsArticle>>>()
      val newsArticles: LiveData<Resource<List<NewsArticle>>> = _newsArticles

      init {
          refreshBreakingNews()
      }

      private fun refreshBreakingNews() {
          viewModelScope.launch {
              _newsArticles.value = Resource.Loading()
              _newsArticles.value = repository.getBreakingNews()
          }
      }

      fun onRefresh() = refreshBreakingNews()*/
}