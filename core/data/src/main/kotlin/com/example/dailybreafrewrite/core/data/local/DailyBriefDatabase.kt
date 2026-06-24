package com.example.dailybreafrewrite.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ArticleEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class DailyBriefDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
