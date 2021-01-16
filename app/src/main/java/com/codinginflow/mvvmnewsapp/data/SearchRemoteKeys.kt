package com.codinginflow.mvvmnewsapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_remote_keys")
data class SearchRemoteKeys(
    @PrimaryKey val articleUrl: String,
    val prevKey: Int?,
    val nextKey: Int?
)