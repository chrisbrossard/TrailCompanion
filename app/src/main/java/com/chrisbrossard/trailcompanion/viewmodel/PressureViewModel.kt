package com.chrisbrossard.trailcompanion.viewmodel


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PressureViewModel : ViewModel() {
    private val _pressure = MutableStateFlow<Float>(0f)
    val pressure: StateFlow<Float> = _pressure

    fun updatePressure(newPressure: Float) {
        _pressure.value = newPressure
    }
}