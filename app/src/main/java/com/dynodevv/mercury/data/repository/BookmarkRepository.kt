package com.dynodevv.mercury.data.repository

import com.dynodevv.mercury.data.local.database.BookmarkDao
import com.dynodevv.mercury.data.local.database.BookmarkEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = bookmarkDao.getAll()

    suspend fun addBookmark(title: String, url: String) {
        bookmarkDao.insert(BookmarkEntity(title = title, url = url))
    }

    suspend fun removeBookmark(url: String) {
        bookmarkDao.deleteByUrl(url)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isBookmarked(url)
    }
}
