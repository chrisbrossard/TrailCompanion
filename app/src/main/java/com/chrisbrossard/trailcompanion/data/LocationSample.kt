package com.chrisbrossard.trailcompanion.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = LocationSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class LocationSample(
    @PrimaryKey(autoGenerate = true) val locationId: Int = 0,
    @ColumnInfo(name = "sessionId") val sessionId: Long, // links to session table
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "x") val x: Float
)