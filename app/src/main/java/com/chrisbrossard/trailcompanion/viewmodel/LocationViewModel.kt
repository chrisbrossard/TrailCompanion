package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.location.Location

class LocationViewModel : ViewModel() {
    private val _location = MutableStateFlow(Location(""))
    val location: StateFlow<Location> = _location

    fun updateLocation(newLocation: Location) {
        _location.value = newLocation
    }
}