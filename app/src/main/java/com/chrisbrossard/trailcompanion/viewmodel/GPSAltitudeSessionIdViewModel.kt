package com.chrisbrossard.trailcompanion.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel

class GPSAltitudeSessionIdViewModel(private val application: Application) : ViewModel() {
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("my_app", Context.MODE_PRIVATE)
    val gPSAltitudeSessionId = sharedPreferences.getLong(
        "gps_altitude_session_id",
        -1L)

    fun setSessionId(value: Long) {
        sharedPreferences.edit {
            putLong("gps_altitude_session_id", value)
        }
    }

    fun getSessionId(): Long {
        return sharedPreferences.getLong("gps_altitude_session_id", -1L)
    }
}