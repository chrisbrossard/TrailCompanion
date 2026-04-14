package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.data.StepSampleDao

class StepCountViewModel(private val dao: StepSampleDao) : ViewModel() {
    val rowCount = dao.getRowCount()
}