package com.codinginflow.mvvmnewsapp.bookmarks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmnewsapp.data.NewsArticle
import com.codinginflow.mvvmnewsapp.data.NewsArticleDao
import com.codinginflow.mvvmnewsapp.data.NewsRepository
import kotlinx.coroutines.launch

class BookmarksViewModel @ViewModelInject constructor(
    private val repository: NewsRepository
) : ViewModel() {
    fun onBookmarkClick(article: NewsArticle) {
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.update(updatedArticle)
        }
    }

    val bookmarks = repository.getAllBookmarkedArticles().asLiveData()
}