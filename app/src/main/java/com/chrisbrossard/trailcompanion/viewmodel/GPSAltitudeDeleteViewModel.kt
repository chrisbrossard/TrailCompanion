package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.viewmodel.GPSAltitudeSessionDao
import com.chrisbrossard.trailcompanion.data.GPSAltitudeSampleDao

class GPSAltitudeDeleteViewModel(
    val sampleDao: GPSAltitudeSampleDao,
    val sessionDao: GPSAltitudeSessionDao
) : ViewModel() {

    fun deleteBySessionId(sessionId: Long) {
        sampleDao.deleteBySessionId(sessionId)
        sessionDao.deleteBySessionId(sessionId)
    }
}