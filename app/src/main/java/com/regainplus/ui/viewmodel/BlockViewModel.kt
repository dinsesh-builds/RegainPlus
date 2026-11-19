package com.regainplus.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.BlockedDao
import com.regainplus.service.BlockHolder
import com.regainplus.data.entity.BlockedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BlockViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val dao: BlockedDao
) : ViewModel() {
    val items: StateFlow<List<BlockedItem>> = dao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            items.collect { list ->
                BlockHolder.set(list.filter { it.kind == "app" }.map { it.name }.toSet(), ctx)
                BlockHolder.setSites(list.filter { it.kind == "site" }.map { it.name }.toSet(), ctx)
                BlockHolder.setStudyAllow(list.filter { it.kind == "study" }.map { it.name }.toSet(), ctx)
            }
        }
    }
    fun add(kind: String, name: String) {
        viewModelScope.launch {
            val clean = if (kind == "site") name.trim().removePrefix("http://").removePrefix("https://").removePrefix("www.").substringBefore("/") else name.trim()
            if (clean.isBlank()) return@launch
            dao.insert(BlockedItem(UUID.randomUUID().toString(), kind, clean, System.currentTimeMillis()))
            when (kind) {
                "study" -> BlockHolder.setStudyAllow((BlockHolder.studyAllow.value + clean).toSet(), ctx)
                "site" -> BlockHolder.setSites((BlockHolder.sites.value + clean).toSet(), ctx)
                else -> BlockHolder.set((BlockHolder.blocked.value + clean).toSet(), ctx)
            }
            com.regainplus.service.BlockMonitorService.sync(ctx)
            com.regainplus.RegainPlusApp.syncWatchdog(ctx)
        }
    }
    fun refreshMonitor() {
        com.regainplus.service.BlockMonitorService.sync(ctx)
        com.regainplus.RegainPlusApp.syncWatchdog(ctx)
    }
    fun clearAll() {
        viewModelScope.launch {
            dao.clearAll()
            BlockHolder.set(emptySet(), ctx)
            BlockHolder.setSites(emptySet(), ctx)
            BlockHolder.setStudyAllow(emptySet(), ctx)
            com.regainplus.service.BlockMonitorService.sync(ctx)
            com.regainplus.RegainPlusApp.syncWatchdog(ctx)
        }
    }
    fun delete(id: String) {
        viewModelScope.launch {
            val item = dao.all().first().firstOrNull { it.id == id } ?: return@launch
            dao.delete(id)
            when (item.kind) {
                "study" -> BlockHolder.setStudyAllow(BlockHolder.studyAllow.value - item.name, ctx)
                "site" -> BlockHolder.setSites(BlockHolder.sites.value - item.name, ctx)
                else -> BlockHolder.set(BlockHolder.blocked.value - item.name, ctx)
            }
            com.regainplus.service.BlockMonitorService.sync(ctx)
            com.regainplus.RegainPlusApp.syncWatchdog(ctx)
        }
    }
}