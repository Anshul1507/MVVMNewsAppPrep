package com.codinginflow.mvvmnewsapp.api

import com.codinginflow.mvvmnewsapp.data.NewsArticle

data class NewsResponse(val articles: List<NewsArticle>)