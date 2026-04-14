package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VerticalSpeedViewModel : ViewModel() {
    private val _verticalSpeed = MutableStateFlow<Float>(0f)
    val verticalSpeed: StateFlow<Float> = _verticalSpeed

    fun updateVerticalSpeed(newVerticalAcceleration: Float) {
        _verticalSpeed.value = newVerticalAcceleration
    }
}