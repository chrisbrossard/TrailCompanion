package com.chrisbrossard.trailcompanion.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel

class StepSessionIdViewModel(private val application: Application) : ViewModel() {
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("my_app", Context.MODE_PRIVATE)
    val stepSessionId = sharedPreferences.getLong("step_session_id", -2L)

    fun setSessionId(value: Long) {
        sharedPreferences.edit {
            putLong("step_session_id", value)
        }
    }

    fun getSessionId(): Long {
        val id = sharedPreferences.getLong("step_session_id", -2L)
        return id
    }
}