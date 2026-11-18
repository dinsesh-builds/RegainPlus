package com.regainplus.util

/** Pure time helpers, unit-testable. */
object TimeUtils {
    /** Whether [now] (minutes since midnight) falls within [start]..[end], supporting wrap-around midnight. */
    fun inWindow(now: Int, start: Int, end: Int): Boolean =
        if (start <= end) now in start..end else now >= start || now <= end
}
