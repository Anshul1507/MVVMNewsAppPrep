package com.codinginflow.mvvmnewsapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SearchRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<SearchRemoteKeys>)

    @Query("SELECT * FROM search_remote_keys WHERE articleUrl = :articleUrl")
    suspend fun getRemoteKeyFromArticleUrl(articleUrl: String): SearchRemoteKeys?

    @Query("DELETE FROM search_remote_keys")
    suspend fun clearRemoteKeys()
}