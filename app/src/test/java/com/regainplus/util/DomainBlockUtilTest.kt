package com.regainplus.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainBlockUtilTest {
    @Test
    fun adultDomainsBlockedOnlyWhenEnabled() {
        assertFalse(DomainBlockUtil.isBlocked("pornhub.com", false, emptySet()))
        assertTrue(DomainBlockUtil.isBlocked("pornhub.com", true, emptySet()))
        assertTrue(DomainBlockUtil.isBlocked("www.xvideos.com", true, emptySet()))
    }

    @Test
    fun customSitesAlwaysBlocked() {
        assertTrue(DomainBlockUtil.isBlocked("example.com/page", false, setOf("example.com")))
        assertTrue(DomainBlockUtil.isBlocked("sub.reddit.com", false, setOf("reddit.com")))
        assertFalse(DomainBlockUtil.isBlocked("harmless.com", true, setOf("example.com")))
    }

    @Test
    fun shortStringsIgnored() {
        assertFalse(DomainBlockUtil.isBlocked("a.b", true, emptySet()))
    }
}
