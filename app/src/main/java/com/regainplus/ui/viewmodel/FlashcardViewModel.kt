package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.FlashcardDao
import com.regainplus.data.entity.Flashcard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FlashcardViewModel @Inject constructor(private val dao: FlashcardDao) : ViewModel() {
    val cards: StateFlow<List<Flashcard>> = dao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val due: StateFlow<List<Flashcard>> = dao.due(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun add(deck: String, front: String, back: String) {
        viewModelScope.launch { dao.insert(Flashcard(UUID.randomUUID().toString(), deck, front, back)) }
    }
    fun review(card: Flashcard, good: Boolean) {
        viewModelScope.launch {
            val newEase = if (good) (card.ease + 0.1f).coerceAtMost(3.0f) else (card.ease - 0.2f).coerceAtLeast(1.3f)
            val newInterval = if (good) (card.interval * newEase).toInt().coerceAtLeast(1) else 1
            dao.update(card.copy(ease = newEase, interval = newInterval, nextReview = System.currentTimeMillis() + newInterval * 86400000L, reviews = card.reviews + 1))
        }
    }
    fun delete(id: String) { viewModelScope.launch { dao.delete(id) } }
}