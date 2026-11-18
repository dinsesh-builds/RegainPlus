package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.OutfitDao
import com.regainplus.data.dao.WardrobeDao
import com.regainplus.data.entity.Outfit
import com.regainplus.data.entity.WardrobeItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OutfitsViewModel @Inject constructor(
    private val outfitDao: OutfitDao,
    private val wardrobeDao: WardrobeDao
) : ViewModel() {
    val outfits: StateFlow<List<Outfit>> = outfitDao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val items: StateFlow<List<WardrobeItem>> = wardrobeDao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun add(name: String, itemIds: String, occasion: String, season: String) {
        viewModelScope.launch { outfitDao.insert(Outfit(UUID.randomUUID().toString(), name, itemIds, occasion, season)) }
    }
    fun markWorn(id: String) { viewModelScope.launch { outfitDao.markWorn(id, System.currentTimeMillis()) } }
    fun delete(id: String) { viewModelScope.launch { outfitDao.delete(id) } }
}