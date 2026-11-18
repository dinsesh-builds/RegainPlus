package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.BodyMetricDao
import com.regainplus.data.dao.CalorieDao
import com.regainplus.data.entity.BodyMetric
import com.regainplus.data.entity.CalorieLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BodyViewModel @Inject constructor(
    private val calorieDao: CalorieDao,
    private val metricDao: BodyMetricDao
) : ViewModel() {
    val calories: StateFlow<List<CalorieLog>> = calorieDao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val metrics: StateFlow<List<BodyMetric>> = metricDao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun addCalorie(meal: String, cal: Int, p: Int, c: Int, f: Int) {
        viewModelScope.launch { calorieDao.insert(CalorieLog(UUID.randomUUID().toString(), meal, cal, p, c, f, System.currentTimeMillis())) }
    }
    fun addWeight(w: Double, note: String) {
        viewModelScope.launch { metricDao.insert(BodyMetric(UUID.randomUUID().toString(), System.currentTimeMillis(), w, note)) }
    }
    fun addPhoto(uri: String) {
        viewModelScope.launch { metricDao.insert(BodyMetric(UUID.randomUUID().toString(), System.currentTimeMillis(), 0.0, "photo", uri)) }
    }
    fun deleteCalorie(id: String) { viewModelScope.launch { calorieDao.delete(id) } }
}