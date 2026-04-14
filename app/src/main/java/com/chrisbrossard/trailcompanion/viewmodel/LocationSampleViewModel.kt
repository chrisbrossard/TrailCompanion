package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.data.AltitudeSample
import com.chrisbrossard.trailcompanion.data.LocationSample
import com.chrisbrossard.trailcompanion.data.LocationSampleDao

class LocationSampleViewModel(private val dao: LocationSampleDao): ViewModel() {
    fun getSample(locationId: Int): LocationSample {
        return dao.findById(locationId)
    }

    fun findByX(x: Float): LocationSample {
        return dao.findByX(x)
    }

    fun setX(sampleId: Int, x: Float) {
        dao.updateX(sampleId, x)
    }
}