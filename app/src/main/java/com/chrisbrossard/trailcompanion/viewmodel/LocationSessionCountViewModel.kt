package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import com.chrisbrossard.trailcompanion.data.AltitudeSessionDao
import com.chrisbrossard.trailcompanion.data.LocationSessionDao
import kotlinx.coroutines.flow.Flow

class LocationSessionCountViewModel(private val dao: LocationSessionDao) : ViewModel() {
    val rowCount = dao.getRowCount()

    fun getCount(): Flow<Int> {
        return dao.getRowCount()
    }
}