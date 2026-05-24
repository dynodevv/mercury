package com.dynodevv.mercury.data.repository

import com.dynodevv.mercury.data.local.database.HistoryDao
import com.dynodevv.mercury.data.local.database.HistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    fun getAllHistory(): Flow<List<HistoryEntity>> = historyDao.getAll()

    fun getRecentHistory(limit: Int): Flow<List<HistoryEntity>> = historyDao.getRecent(limit)

    suspend fun addHistoryItem(title: String, url: String) {
        historyDao.insert(HistoryEntity(title = title, url = url))
    }

    suspend fun clearHistory() {
        historyDao.clearAll()
    }

    suspend fun deleteHistoryItem(item: HistoryEntity) {
        historyDao.delete(item)
    }
}
