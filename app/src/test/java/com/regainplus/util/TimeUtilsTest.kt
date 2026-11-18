package com.regainplus.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeUtilsTest {
    @Test
    fun inWindow_normalRange() {
        assertTrue(TimeUtils.inWindow(600, 540, 720))   // 10:00 in 09:00-12:00
        assertFalse(TimeUtils.inWindow(480, 540, 720))  // 08:00 not in
        assertTrue(TimeUtils.inWindow(540, 540, 720))   // boundary start
        assertTrue(TimeUtils.inWindow(720, 540, 720))   // boundary end
    }

    @Test
    fun inWindow_wrapAroundMidnight() {
        assertTrue(TimeUtils.inWindow(1380, 1320, 60))  // 23:00 in 22:00-01:00
        assertTrue(TimeUtils.inWindow(30, 1320, 60))     // 00:30 in
        assertFalse(TimeUtils.inWindow(720, 1320, 60))  // 12:00 not in
    }
}
