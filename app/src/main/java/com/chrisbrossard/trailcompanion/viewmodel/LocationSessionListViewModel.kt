package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisbrossard.trailcompanion.data.AltitudeSessionDao
import com.chrisbrossard.trailcompanion.data.LocationSessionDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class LocationSessionListViewModel(private val dao: LocationSessionDao) : ViewModel() {
    val rowList = dao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}