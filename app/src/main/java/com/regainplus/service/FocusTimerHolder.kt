package com.regainplus.service

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** BUG-03 fix: timer state lives outside the composable, survives navigation and process death. */
object FocusTimerHolder {
    private const val P = "focus_timer"
    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running
    private val _isBreak = MutableStateFlow(false)
    val isBreak: StateFlow<Boolean> = _isBreak
    private val _remaining = MutableStateFlow(25 * 60)
    val remaining: StateFlow<Int> = _remaining
    private val _focusMin = MutableStateFlow(25)
    val focusMin: StateFlow<Int> = _focusMin
    private val _breakMin = MutableStateFlow(5)
    val breakMin: StateFlow<Int> = _breakMin
    private val _strict = MutableStateFlow(false)
    val strict: StateFlow<Boolean> = _strict
    private val _pomodoro = MutableStateFlow(true)
    val pomodoro: StateFlow<Boolean> = _pomodoro

    private var startMs = 0L
    private var appCtx: Context? = null
    private var pendingFocusComplete = false

    /** Start time of a focus that finished while the app was closed, or 0. */
    fun consumePendingFocus(): Long {
        if (!pendingFocusComplete) return 0L
        pendingFocusComplete = false
        return lastCompletedStart
    }
    private var lastCompletedStart = 0L

    fun init(ctx: Context) {
        appCtx = ctx.applicationContext
        val p = ctx.getSharedPreferences(P, Context.MODE_PRIVATE)
        _focusMin.value = p.getInt("focusMin", 25)
        _breakMin.value = p.getInt("breakMin", 5)
        _strict.value = p.getBoolean("strict", false)
        _pomodoro.value = p.getBoolean("pomodoro", true)
        _isBreak.value = p.getBoolean("isBreak", false)
        _running.value = p.getBoolean("running", false)
        startMs = p.getLong("startMs", 0L)
        val total = (if (_isBreak.value) _breakMin.value else _focusMin.value) * 60
        _remaining.value = if (_running.value && startMs > 0) (total - (System.currentTimeMillis() - startMs) / 1000).toInt().coerceAtLeast(0) else total
        if (_running.value && _remaining.value <= 0) {
            lastCompletedStart = startMs
            pendingFocusComplete = true
            completeFocus(ctx)
        }
        if (_running.value) {
            try { ctx.startForegroundService(Intent(ctx, FocusTimerService::class.java)) } catch (_: Exception) {}
        }
    }

    private fun save(ctx: Context) {
        ctx.getSharedPreferences(P, Context.MODE_PRIVATE).edit()
            .putBoolean("running", _running.value).putBoolean("isBreak", _isBreak.value)
            .putInt("focusMin", _focusMin.value).putInt("breakMin", _breakMin.value)
            .putBoolean("strict", _strict.value).putBoolean("pomodoro", _pomodoro.value)
            .putLong("startMs", startMs).apply()
    }

    fun setFocusMin(ctx: Context, v: Int) { if (_running.value && !_isBreak.value) return; _focusMin.value = v; save(ctx); reset(ctx) }
    fun setBreakMin(ctx: Context, v: Int) { if (_running.value && _isBreak.value) return; _breakMin.value = v; save(ctx); reset(ctx) }
    fun setPomodoro(ctx: Context, v: Boolean) { _pomodoro.value = v; save(ctx) }
    fun setStrict(ctx: Context, v: Boolean) { if (_running.value && !_isBreak.value) return; _strict.value = v; save(ctx) }

    private fun reset(ctx: Context) {
        _running.value = false; startMs = 0L
        _remaining.value = (if (_isBreak.value) _breakMin.value else _focusMin.value) * 60
        save(ctx)
    }

    fun start(ctx: Context) {
        if (_running.value) return
        _remaining.value = (if (_isBreak.value) _breakMin.value else _focusMin.value) * 60
        startMs = System.currentTimeMillis()
        _running.value = true
        save(ctx)
        try { ctx.startForegroundService(Intent(ctx, FocusTimerService::class.java)) } catch (_: Exception) { try { ctx.startService(Intent(ctx, FocusTimerService::class.java)) } catch (_: Exception) {} }
    }

    /** Recompute remaining from wall-clock time. Keeps the timer correct after
     *  the app is backgrounded (the UI tick loop stops when the screen is gone). */
    fun syncRemaining(ctx: Context) {
        if (!_running.value || startMs <= 0) return
        val total = (if (_isBreak.value) _breakMin.value else _focusMin.value) * 60
        val elapsed = (System.currentTimeMillis() - startMs) / 1000
        _remaining.value = (total - elapsed).toInt().coerceAtLeast(0)
    }

    /** Returns true if a focus session was completed (so UI can persist it). */
    fun tick(ctx: Context): Boolean {
        if (!_running.value) return false
        syncRemaining(ctx)
        if (_remaining.value > 0) return false
        return completeFocus(ctx)
    }

    private fun completeFocus(ctx: Context): Boolean {
        val wasFocus = !_isBreak.value
        if (wasFocus) {
            // Remember the session so StudyScreen can persist it when it next opens.
            lastCompletedStart = startMs
            pendingFocusComplete = true
            if (_pomodoro.value) {
                _isBreak.value = true
                _remaining.value = _breakMin.value * 60
                startMs = System.currentTimeMillis()
                save(ctx)
            } else stop(ctx)
        } else {
            _isBreak.value = false
            stop(ctx)
        }
        return wasFocus
    }

    /** Advance the phase when time is up while only the service is running. */
    fun syncComplete(ctx: Context) { completeFocus(ctx) }

    fun stop(ctx: Context) {
        _running.value = false; startMs = 0L
        _remaining.value = (if (_isBreak.value) _breakMin.value else _focusMin.value) * 60
        save(ctx)
        try { ctx.stopService(Intent(ctx, FocusTimerService::class.java)) } catch (_: Exception) {}
    }

    val canStop: Boolean get() = !_strict.value || _isBreak.value
    val sessionStartMs: Long get() = startMs
}
