package com.chrisbrossard.trailcompanion.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AltitudeSessionDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(altitudeSession: AltitudeSession): Long

    @Query("SELECT * FROM altitude_sessions WHERE sessionId = (:sessionId)")
    fun findById(sessionId: Int): AltitudeSession

    @Query("SELECT * FROM altitude_sessions ORDER BY startTime")
    fun getAll(): Flow<List<AltitudeSession>>

    @Query("SELECT * FROM altitude_sessions ORDER BY startTime ASC LIMIT 1")
    fun getFirst(): Flow<AltitudeSession>

    @Query("SELECT COUNT(*) FROM altitude_sessions")
    fun getRowCount(): Flow<Int>

    @Query("DELETE FROM altitude_sessions WHERE sessionId = :sessionId")
    fun deleteBySessionId(sessionId: Long)
}