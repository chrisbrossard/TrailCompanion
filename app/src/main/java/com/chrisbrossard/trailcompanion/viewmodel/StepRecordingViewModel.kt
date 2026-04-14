package com.chrisbrossard.trailcompanion.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.MainActivity

class StepRecordingViewModel(private val application: Application) : ViewModel() {
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("my_app", Context.MODE_PRIVATE)
    var recording = sharedPreferences.getInt("step_recording",
        MainActivity.Recording.OFF.ordinal)
    init {
        sharedPreferences.edit {
            putInt("step_recording", MainActivity.Recording.OFF.ordinal)
        }
    }
    fun updateRecording(value: Int) {
        val sharedPreferences: SharedPreferences =
            application.getSharedPreferences("my_app", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putInt("step_recording", value)
        }
    }
}