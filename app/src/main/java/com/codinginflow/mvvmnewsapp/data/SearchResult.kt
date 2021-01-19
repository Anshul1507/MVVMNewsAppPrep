package com.codinginflow.mvvmnewsapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "search_results", primaryKeys = ["searchQuery", "articleUrl"])
// Foreign key on articleUrl deletes articles out of previous search queries on REPLACE
data class SearchResult(
    val searchQuery: String,
    val articleUrl: String,
    val prevPageKey: Int?,
    val nextPageKey: Int?,
    val queryPosition: Int
)