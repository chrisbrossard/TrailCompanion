package com.chrisbrossard.trailcompanion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AltitudeSampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(altitude: AltitudeSample)

    @Query("SELECT * FROM altitudes WHERE altitudeId = (:altitudeSampleId)")
    fun findById(altitudeSampleId: Int): AltitudeSample

    @Query("SELECT * FROM altitudes ORDER BY time")
    fun getAll(): Flow<List<AltitudeSample>>

    @Query("SELECT * FROM altitudes ORDER BY time ASC LIMIT 1")
    fun getFirst(): Flow<AltitudeSample>

    @Query("SELECT COUNT(*) FROM altitudes")
    fun getRowCount(): Flow<Int>

    @Query("DELETE FROM altitudes WHERE sessionId = :sessionId")
    fun deleteBySessionId(sessionId: Long)

    @Delete
    fun delete(altitudeSample: AltitudeSample)
}