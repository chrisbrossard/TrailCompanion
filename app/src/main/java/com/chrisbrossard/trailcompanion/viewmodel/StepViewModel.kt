package com.chrisbrossard.trailcompanion.viewmodel


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepViewModel : ViewModel() {
    private val _steps = MutableStateFlow<Float>(0f)
    val steps: StateFlow<Float> = _steps

    fun updateSteps(newHeading: Float) {
        _steps.value = newHeading
    }
}