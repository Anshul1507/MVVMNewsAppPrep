package com.codinginflow.mvvmnewsapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NewsArticle::class, SearchResult::class], version = 1)
@TypeConverters(Converters::class)
abstract class NewsArticleDatabase : RoomDatabase() {

    abstract fun newsArticleDao(): NewsArticleDao

    abstract fun searchQueryDao(): SearchResultDao
}