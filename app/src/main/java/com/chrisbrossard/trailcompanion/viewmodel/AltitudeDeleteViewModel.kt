package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.data.AltitudeSampleDao
import com.chrisbrossard.trailcompanion.data.AltitudeSessionDao

class AltitudeDeleteViewModel(
    val sampleDao: AltitudeSampleDao,
    val sessionDao: AltitudeSessionDao) : ViewModel() {

    fun deleteBySessionId(sessionId: Long) {
        sampleDao.deleteBySessionId(sessionId)
        sessionDao.deleteBySessionId(sessionId)
    }
}