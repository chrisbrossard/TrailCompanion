package com.chrisbrossard.trailcompanion.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = StepSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("sessionId")])
data class StepSample(
    @PrimaryKey(autoGenerate = true) val stepId: Int = 0,
    @ColumnInfo(name = "sessionId") val sessionId: Long, // links to session table
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "steps") val steps: Long
)