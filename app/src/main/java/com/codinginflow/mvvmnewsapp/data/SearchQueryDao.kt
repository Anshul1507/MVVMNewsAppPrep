package com.codinginflow.mvvmnewsapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface SearchQueryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchQuery: SearchQuery)
}