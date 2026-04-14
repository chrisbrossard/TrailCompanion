package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import android.location.Location

class NavigationViewModel: ViewModel() {
    var location = Location("")
    var navigating = false

    fun setWaypoint(latitude: Double, longitude: Double) {
        location.latitude = latitude
        location.longitude = longitude
        navigating = true
    }

    fun getWaypoint(): Location {
        return location
    }
}