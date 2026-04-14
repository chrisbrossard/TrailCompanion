package com.chrisbrossard.trailcompanion.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_altitude_sessions")
data class GPSAltitudeSession(
    @PrimaryKey(autoGenerate = true) val sessionId: Long = 0L,
    @ColumnInfo(name = "startTime") val startTime: Long,
    @ColumnInfo(name = "endTime") val endTime: Long
)