package com.dynodevv.mercury.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
}
