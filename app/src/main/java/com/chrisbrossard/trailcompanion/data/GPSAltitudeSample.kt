package com.chrisbrossard.trailcompanion.data


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gps_altitudes",
    foreignKeys = [
        ForeignKey(
            entity = GPSAltitudeSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class GPSAltitudeSample(
    @PrimaryKey(autoGenerate = true) val altitudeId: Int = 0,
    @ColumnInfo(name = "sessionId") val sessionId: Long, // links to session table
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "altitude") val altitude: Float
)