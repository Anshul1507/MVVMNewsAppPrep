package com.codinginflow.mvvmnewsapp.api

data class NewsResponse(val response: ResponseData) {

    data class ResponseData(val results: List<NewsArticleDto>)
}