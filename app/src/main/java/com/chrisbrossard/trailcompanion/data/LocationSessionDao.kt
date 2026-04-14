package com.chrisbrossard.trailcompanion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationSessionDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(locationSession: LocationSession): Long

    @Query("SELECT * FROM location_sessions WHERE sessionId = (:sessionId)")
    fun findById(sessionId: Int): LocationSession

    @Query("SELECT * FROM location_sessions ORDER BY startTime")
    fun getAll(): Flow<List<LocationSession>>

    @Query("SELECT * FROM location_sessions ORDER BY startTime ASC LIMIT 1")
    fun getFirst(): Flow<LocationSession>

    @Query("SELECT COUNT(*) FROM location_sessions")
    fun getRowCount(): Flow<Int>

    @Query("DELETE FROM location_sessions WHERE sessionId = :sessionId")
    fun deleteBySessionId(sessionId: Long)
}