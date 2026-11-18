package com.regainplus.util

import android.content.Context

/** BlockerHero-style keyword filter with whitelist (e.g. block "adult" but allow "adult education"). */
object ContentFilter {
    const val PREFS = "content_filter"

    val DEFAULT_KEYWORDS = listOf(
        "porn", "xxx", "xvideos", "pornhub", "redtube", "xnxx", "xhamster",
        "youporn", "tube8", "spankwire", "sex.com", "nude", "erotic", "escort",
        "porno", "livecam", "webcam", "naked", "fetish", "adult video", "adult site"
    )

    fun keywords(ctx: Context): Set<String> {
        val s = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("keywords", null)
            ?: return DEFAULT_KEYWORDS.toSet()
        return s.split(",").map { it.trim().lowercase() }.filter { it.length >= 3 }.toSet()
    }

    fun whitelist(ctx: Context): Set<String> =
        (ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("whitelist", "") ?: "")
            .split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()

    fun save(ctx: Context, keywords: String, whitelist: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("keywords", keywords).putString("whitelist", whitelist).apply()
    }

    /** True if [text] contains a blocked keyword that isn't rescued by a whitelist entry. */
    fun shouldBlock(text: String, keywords: Set<String>, whitelist: Set<String>): Boolean {
        val lower = text.lowercase()
        if (lower.isBlank()) return false
        for (k in keywords) {
            if (!lower.contains(k)) continue
            // whitelist entry containing this keyword means allow (e.g. "adult education")
            if (whitelist.any { it.contains(k) && lower.contains(it) }) continue
            return true
        }
        return false
    }
}
