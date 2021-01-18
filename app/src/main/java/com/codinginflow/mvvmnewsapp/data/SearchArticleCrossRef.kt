package com.codinginflow.mvvmnewsapp.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation

// TODO: 17.01.2021 Add foreign key constraints with cascade delete

@Entity(tableName = "article_search_cross_refs", primaryKeys = ["articleUrl", "searchQuery"])
data class SearchArticleCrossRef(
     val articleUrl: String,
     val searchQuery: String,
     val prevKey: Int?,
     val nextKey: Int?,
)