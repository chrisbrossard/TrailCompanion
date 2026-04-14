package com.chrisbrossard.trailcompanion.viewmodel


import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel

class LocationSessionIdViewModel(private val application: Application) : ViewModel() {
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("my_app", Context.MODE_PRIVATE)
    val locationSessionId = sharedPreferences.getLong("location_session_id", -1L)

    fun setSessionId(value: Long) {
        sharedPreferences.edit {
            putLong("location_session_id", value)
        }
    }

    fun getSessionId(): Long {
        val id = sharedPreferences.getLong("location_session_id", -2L)
        return id
    }
}