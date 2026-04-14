package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.data.StepSampleDao
import com.chrisbrossard.trailcompanion.data.StepSessionDao

class StepDeleteViewModel(
    val sampleDao: StepSampleDao,
    val sessionDao: StepSessionDao) : ViewModel() {

    fun deleteBySessionId(sessionId: Long) {
        sampleDao.deleteBySessionId(sessionId)
        sessionDao.deleteBySessionId(sessionId)
    }
}