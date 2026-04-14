package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HeadingViewModel : ViewModel() {
    private val _heading = MutableStateFlow<Float>(0f)
    val heading: StateFlow<Float> = _heading

    fun updateHeading(newHeading: Float) {
        _heading.value = newHeading
    }
}
