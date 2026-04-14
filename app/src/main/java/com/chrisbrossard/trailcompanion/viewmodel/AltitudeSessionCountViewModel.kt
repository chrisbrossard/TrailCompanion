package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.data.AltitudeSessionDao
import kotlinx.coroutines.flow.Flow

class AltitudeSessionCountViewModel(private val dao: AltitudeSessionDao) : ViewModel() {
    val rowCount = dao.getRowCount()

    fun getCount(): Flow<Int> {
        return dao.getRowCount()
    }
}
