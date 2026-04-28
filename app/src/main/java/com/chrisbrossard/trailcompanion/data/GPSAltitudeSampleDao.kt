package com.chrisbrossard.trailcompanion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GPSAltitudeSampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(gPSAltitude: GPSAltitudeSample)

    @Query("SELECT * FROM gps_altitudes WHERE altitudeId = (:gPSAltitudeSampleId)")
    fun findById(gPSAltitudeSampleId: Int): GPSAltitudeSample

    @Query("SELECT * FROM gps_altitudes ORDER BY time")
    fun getAll(): Flow<List<GPSAltitudeSample>>

    @Query("SELECT * FROM gps_altitudes ORDER BY time ASC LIMIT 1")
    fun getFirst(): Flow<GPSAltitudeSample>

    @Query("SELECT COUNT(*) FROM gps_altitudes")
    fun getRowCount(): Flow<Int>

    @Query("DELETE FROM gps_altitudes WHERE sessionId = :sessionId")
    fun deleteBySessionId(sessionId: Long)

    @Delete
    fun delete(gPSAltitudeSample: GPSAltitudeSample)
}