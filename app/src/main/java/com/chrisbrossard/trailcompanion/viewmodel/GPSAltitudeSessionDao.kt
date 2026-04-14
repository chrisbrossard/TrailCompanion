package com.chrisbrossard.trailcompanion.viewmodel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chrisbrossard.trailcompanion.data.GPSAltitudeSession
import kotlinx.coroutines.flow.Flow

@Dao
interface GPSAltitudeSessionDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(gPSAltitudeSession: GPSAltitudeSession): Long

    @Query("SELECT * FROM gps_altitude_sessions WHERE sessionId = (:sessionId)")
    fun findById(sessionId: Int): GPSAltitudeSession

    @Query("SELECT * FROM gps_altitude_sessions ORDER BY startTime")
    fun getAll(): Flow<List<GPSAltitudeSession>>

    @Query("SELECT * FROM gps_altitude_sessions ORDER BY startTime ASC LIMIT 1")
    fun getFirst(): Flow<GPSAltitudeSession>

    @Query("SELECT COUNT(*) FROM gps_altitude_sessions")
    fun getRowCount(): Flow<Int>

    @Query("DELETE FROM gps_altitude_sessions WHERE sessionId = :sessionId")
    fun deleteBySessionId(sessionId: Long)
}