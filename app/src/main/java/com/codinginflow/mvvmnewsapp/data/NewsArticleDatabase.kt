package com.codinginflow.mvvmnewsapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NewsArticle::class, SearchQuery::class, SearchArticleCrossRef::class],
    version = 1
)
abstract class NewsArticleDatabase : RoomDatabase() {

    abstract fun newsArticleDao(): NewsArticleDao

    abstract fun searchQueryArticlesDao(): SearchArticleCrossRefDao

    abstract fun searchQueryDao(): SearchQueryDao
}