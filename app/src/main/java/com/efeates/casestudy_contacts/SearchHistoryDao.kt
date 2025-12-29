package com.efeates.casestudy_contacts

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getAllSearchHistory(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(searchHistory: SearchHistory)

    @Query("DELETE FROM search_history")
    suspend fun deleteAllSearchHistory()

    @Delete
    suspend fun deleteSearchItem(searchHistory: SearchHistory)
}
