package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.TaskDao
import com.regainplus.data.entity.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(private val dao: TaskDao) : ViewModel() {
    val tasks: StateFlow<List<Task>> = dao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun add(title: String) { viewModelScope.launch { dao.insert(Task(UUID.randomUUID().toString(), title)) } }
    fun toggle(t: Task) { viewModelScope.launch { dao.update(t.copy(done = !t.done)) } }
    fun delete(id: String) { viewModelScope.launch { dao.delete(id) } }
}