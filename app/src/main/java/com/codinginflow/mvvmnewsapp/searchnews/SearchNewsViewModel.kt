package com.codinginflow.mvvmnewsapp.searchnews

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmnewsapp.api.NewsApi
import com.codinginflow.mvvmnewsapp.data.NewsArticle
import com.codinginflow.mvvmnewsapp.data.NewsRepository
import com.codinginflow.mvvmnewsapp.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsViewModel @ViewModelInject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _newsArticles = MutableLiveData<Resource<List<NewsArticle>>>()
    val newsArticles: LiveData<Resource<List<NewsArticle>>> = _newsArticles

    fun findNews(query: String) {
        viewModelScope.launch {
            _newsArticles.value = Resource.Loading()
            val result = repository.searchNews(query)
            _newsArticles.value = result
        }
    }

    fun onBookmarkClick(article: NewsArticle) {
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.update(updatedArticle)
        }
    }
}