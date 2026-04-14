package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GPSAltitudeViewModel : ViewModel() {
    private val _altitude = MutableStateFlow<Double>(0.0)
    val altitude: StateFlow<Double> = _altitude

    fun updateAltitude(newAltitude: Double) {
        _altitude.value = newAltitude
    }
}