package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.data.StepSessionDao

class StepSessionCountViewModel(private val dao: StepSessionDao) : ViewModel() {
    val rowCount = dao.getRowCount()
}