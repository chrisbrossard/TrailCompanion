package com.chrisbrossard.trailcompanion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepSessionDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(stepSession: StepSession): Long

    @Query("SELECT * FROM step_sessions WHERE sessionId = (:stepSessionId)")
    fun findById(stepSessionId: Int): StepSession

    @Query("SELECT * FROM step_sessions ORDER BY startTime")
    fun getAll(): Flow<List<StepSession>>

    @Query("SELECT * FROM step_sessions ORDER BY startTime ASC LIMIT 1")
    fun getFirst(): Flow<StepSession>

    @Query("SELECT COUNT(*) FROM step_sessions")
    fun getRowCount(): Flow<Int>

    @Query("DELETE FROM step_sessions WHERE sessionId = :sessionId")
    fun deleteBySessionId(sessionId: Long)
}