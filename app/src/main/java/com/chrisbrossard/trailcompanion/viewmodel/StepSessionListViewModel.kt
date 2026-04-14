package com.chrisbrossard.trailcompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisbrossard.trailcompanion.data.StepSessionDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class StepSessionListViewModel(private val dao: StepSessionDao) : ViewModel() {
    val rowList = dao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}