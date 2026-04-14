package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

class GPSAltitudeSessionCountViewModel(private val dao: GPSAltitudeSessionDao) : ViewModel() {
    val rowCount = dao.getRowCount()

    fun getCount(): Flow<Int> {
        return dao.getRowCount()
    }
}