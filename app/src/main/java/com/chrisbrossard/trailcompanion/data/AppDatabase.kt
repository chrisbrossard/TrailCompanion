package com.chrisbrossard.trailcompanion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionDao

@Database(
    entities = [
        StepSample::class,
        AltitudeSample::class,
        GPSAltitudeSample::class,
        LocationSample::class,
        StepSession::class,
        AltitudeSession::class,
        GPSAltitudeSession::class,
        LocationSession::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepSampleDao(): StepSampleDao
    abstract fun altitudeSampleDao(): AltitudeSampleDao
    abstract fun gPSAltitudeSampleDao(): GPSAltitudeSampleDao
    abstract fun locationSampleDao(): LocationSampleDao
    abstract fun stepSessionDao(): StepSessionDao
    abstract fun altitudeSessionDao(): AltitudeSessionDao
    abstract fun gPSAltitudeSessionDao(): GPSAltitudeSessionDao
    abstract fun locationSessionDao(): LocationSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lc_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}