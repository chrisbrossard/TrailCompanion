package com.chrisbrossard.trailcompanion.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.core.content.edit

class AltitudeSessionIdViewModel(private val application: Application) : ViewModel() {
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("my_app", Context.MODE_PRIVATE)
    val altitudeSessionId = sharedPreferences.getLong("altitude_session_id", -1L)

    fun setSessionId(value: Long) {
        sharedPreferences.edit {
            putLong("altitude_session_id", value)
        }
    }

    fun getSessionId(): Long {
        return sharedPreferences.getLong("altitude_session_id", -1L)
    }
}