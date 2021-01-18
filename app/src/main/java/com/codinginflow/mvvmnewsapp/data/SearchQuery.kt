package com.codinginflow.mvvmnewsapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_queries")
data class SearchQuery(
    @PrimaryKey val searchQuery: String
)