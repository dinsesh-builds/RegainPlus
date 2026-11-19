package com.regainplus.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocusAlarmSchedulerTest {
    @Test
    fun alarm_serialize_deserialize_roundTrip() {
        val alarm = FocusAlarmScheduler.Alarm(slot = 2, hour = 13, minute = 45, days = "1010100")
        val encoded = alarm.serialize()
        assertEquals("2|13|45|1010100", encoded)

        val decoded = FocusAlarmScheduler.Alarm.deserialize(encoded)
        assertEquals(alarm, decoded)
    }

    @Test
    fun alarm_deserialize_invalid_returnsNull() {
        assertNull(FocusAlarmScheduler.Alarm.deserialize(""))
        assertNull(FocusAlarmScheduler.Alarm.deserialize("1|2|3"))
        assertNull(FocusAlarmScheduler.Alarm.deserialize("bad"))
    }

    @Test
    fun alarm_deserialize_malformedNumbers_fallsBackSafely() {
        val decoded = FocusAlarmScheduler.Alarm.deserialize("x|y|z|1111111")
        assertEquals(FocusAlarmScheduler.Alarm(slot = 0, hour = 9, minute = 0, days = "1111111"), decoded)
    }
}