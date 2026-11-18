package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.GymDao
import com.regainplus.data.entity.GymLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GymViewModel @Inject constructor(private val dao: GymDao) : ViewModel() {
    val logs: StateFlow<List<GymLog>> = dao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun add(exercise: String, sets: Int, reps: Int, weight: Double) = addAt(exercise, sets, reps, weight, System.currentTimeMillis())
    fun addAt(exercise: String, sets: Int, reps: Int, weight: Double, date: Long) {
        viewModelScope.launch { dao.insert(GymLog(UUID.randomUUID().toString(), exercise, sets, reps, weight, date)) }
    }
    fun delete(id: String) { viewModelScope.launch { dao.delete(id) } }
}