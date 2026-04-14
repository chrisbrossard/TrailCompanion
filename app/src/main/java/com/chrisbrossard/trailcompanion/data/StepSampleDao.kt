package com.chrisbrossard.trailcompanion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepSampleDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(steps: StepSample)

    @Query("SELECT * FROM steps WHERE stepId = (:stepSampleId)")
    fun findById(stepSampleId: Int): StepSample

    @Query("SELECT * FROM steps ORDER BY time")
    fun getAll(): Flow<List<StepSample>>

    @Query("SELECT COUNT(*) FROM steps")
    fun getRowCount(): Flow<Int>

    @Query("DELETE FROM steps")
    fun clearTable()

    @Query("DELETE FROM steps WHERE sessionId = :sessionId")
    fun deleteBySessionId(sessionId: Long)

    @Delete
    fun delete(step: StepSample)
}