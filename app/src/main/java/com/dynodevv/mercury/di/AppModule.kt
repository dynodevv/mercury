package com.dynodevv.mercury.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.dynodevv.mercury.data.local.database.AppDatabase
import com.dynodevv.mercury.data.local.datastore.SettingsDataStore
import com.dynodevv.mercury.data.remote.api.SearchClient
import com.dynodevv.mercury.data.repository.AiRepository
import com.dynodevv.mercury.data.repository.BookmarkRepository
import com.dynodevv.mercury.data.repository.HistoryRepository
import com.dynodevv.mercury.data.repository.SettingsRepository
import com.dynodevv.mercury.service.adblock.AdBlockService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.NONE
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("settings")
        }
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(dataStore: DataStore<Preferences>): SettingsDataStore {
        return SettingsDataStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsDataStore: SettingsDataStore): SettingsRepository {
        return SettingsRepository(settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mercury_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase) = database.historyDao()

    @Provides
    @Singleton
    fun provideBookmarkDao(database: AppDatabase) = database.bookmarkDao()

    @Provides
    @Singleton
    fun provideHistoryRepository(historyDao: com.dynodevv.mercury.data.local.database.HistoryDao): HistoryRepository {
        return HistoryRepository(historyDao)
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(bookmarkDao: com.dynodevv.mercury.data.local.database.BookmarkDao): BookmarkRepository {
        return BookmarkRepository(bookmarkDao)
    }

    @Provides
    @Singleton
    fun provideSearchClient(httpClient: HttpClient): SearchClient {
        return SearchClient(httpClient)
    }

    @Provides
    @Singleton
    fun provideAiRepository(httpClient: HttpClient, settingsDataStore: SettingsDataStore): AiRepository {
        return AiRepository(httpClient, settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideAdBlockService(@ApplicationContext context: Context): AdBlockService {
        return AdBlockService(context)
    }

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
