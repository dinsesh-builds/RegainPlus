package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.WardrobeDao
import com.regainplus.data.entity.WardrobeItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WardrobeViewModel @Inject constructor(private val dao: WardrobeDao) : ViewModel() {
    val items: StateFlow<List<WardrobeItem>> = dao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun add(name: String, type: String, color: String, season: String) {
        viewModelScope.launch { dao.insert(WardrobeItem(UUID.randomUUID().toString(), name, type, color, season)) }
    }
    fun markWorn(id: String) { viewModelScope.launch { dao.markWorn(id, System.currentTimeMillis()) } }
    fun delete(id: String) { viewModelScope.launch { dao.delete(id) } }
}