package com.chrisbrossard.trailcompanion.viewmodel

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.hardware.SensorManager.PRESSURE_STANDARD_ATMOSPHERE
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.MainActivity
import com.chrisbrossard.trailcompanion.MainActivity.Recording
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SeaLevelPressureViewModel() : ViewModel() {
    private val _pressure = MutableStateFlow<Float>(0f)
    val pressure: StateFlow<Float> = _pressure
    init {
        _pressure.value = PRESSURE_STANDARD_ATMOSPHERE
    }

    fun updatePressure(newPressure: Float) {
        _pressure.value = newPressure
    }
}