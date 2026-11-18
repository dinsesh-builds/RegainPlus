package com.regainplus.util

object DomainBlockUtil {
    private val ADULT = listOf(
        "porn", "pornhub", "xvideos", "xnxx", "xhamster", "redtube", "youporn",
        "tube8", "spankwire", "sex", "nude", "erotic", "escort", "porno",
        "livecam", "webcam", "naked", "fetish", "adult", "anal", "orgy",
        "milf", "xxx", "stripchat", "chaturbate", "onlyfans", "brazzers"
    )

    /** Match adult keywords on domain-label boundaries (avoids 'unisex' false positives). */
    fun isAdultDomain(domain: String): Boolean {
        val d = domain.lowercase().removePrefix("www.").trimEnd('.').substringBefore("/")
        if (d.length < 4) return false
        val labels = d.split('.')
        return labels.any { label -> ADULT.any { a -> label == a || label.startsWith(a) } }
    }

    fun isBlocked(domain: String, adultOn: Boolean, customSites: Set<String>): Boolean {
        val d = domain.lowercase().removePrefix("www.").substringBefore("/")
        if (d.length < 4) return false
        if (customSites.any { d.contains(it.lowercase()) }) return true
        return adultOn && isAdultDomain(d)
    }
}
