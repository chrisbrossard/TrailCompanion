package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChartDistanceViewModel : ViewModel() {
    private val _distance = MutableStateFlow<Float>(0f)
    var distance: StateFlow<Float> = _distance

    fun updateDistance(newDistance: Float) {
        _distance.value += newDistance
    }
}