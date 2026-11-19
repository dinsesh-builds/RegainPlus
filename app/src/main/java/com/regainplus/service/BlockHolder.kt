package com.regainplus.service

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// In-memory mirrors of blocked packages and sites, persisted in SharedPreferences
// so the accessibility service works even when the Block screen / BlockViewModel is not alive.
object BlockHolder {
    private const val PREFS = "settings"
    private val _blocked = MutableStateFlow<Set<String>>(emptySet())
    val blocked: StateFlow<Set<String>> = _blocked
    private val _sites = MutableStateFlow<Set<String>>(emptySet())
    val sites: StateFlow<Set<String>> = _sites
    private val _studyMode = MutableStateFlow(false)
    val studyMode: StateFlow<Boolean> = _studyMode
    private val _studyAllow = MutableStateFlow<Set<String>>(emptySet())
    val studyAllow: StateFlow<Set<String>> = _studyAllow

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val blockedPkgs = prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()
        val blockedSites = prefs.getStringSet("blocked_sites", emptySet()) ?: emptySet()
        val studyModeOn = prefs.getBoolean("study_mode", false)
        val studyAllowPkgs = prefs.getStringSet("study_allow", emptySet()) ?: emptySet()

        if (_blocked.value != blockedPkgs) _blocked.value = blockedPkgs
        if (_sites.value != blockedSites) _sites.value = blockedSites
        if (_studyMode.value != studyModeOn) _studyMode.value = studyModeOn
        if (_studyAllow.value != studyAllowPkgs) _studyAllow.value = studyAllowPkgs
    }

    fun set(names: Set<String>, context: Context? = null) {
        _blocked.value = names
        context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()?.putStringSet("blocked_packages", names)?.apply()
    }

    fun setSites(names: Set<String>, context: Context? = null) {
        _sites.value = names
        context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()?.putStringSet("blocked_sites", names)?.apply()
    }

    fun setStudyMode(v: Boolean, context: Context? = null) {
        _studyMode.value = v
        context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putBoolean("study_mode", v)?.apply()
    }

    fun setStudyAllow(names: Set<String>, context: Context? = null) {
        _studyAllow.value = names
        context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putStringSet("study_allow", names)?.apply()
    }

    fun isStudyModeActive(): Boolean = _studyMode.value
    fun isStudyAllowed(pkg: String): Boolean = _studyAllow.value.any { pkg == it || pkg.startsWith(it) }

    /** Study mode is a whitelist: when ON, block everything except the study allowlist. */
    fun isBlocked(pkg: String): Boolean {
        if (_studyMode.value) return !isStudyAllowed(pkg)
        return _blocked.value.contains(pkg)
    }
    fun isSiteBlocked(domain: String): Boolean {
        val d = domain.lowercase()
        return _sites.value.any { site -> d.contains(site.lowercase()) }
    }
}
