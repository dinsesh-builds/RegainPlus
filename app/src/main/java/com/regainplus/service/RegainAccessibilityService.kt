package com.regainplus.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * BlockerHero-style blocker: an AccessibilityService that detects short-form
 * video by the actual view ids / activity classes short-form UIs expose, then
 * immediately backs out and covers the screen with a blocking overlay.
 *
 * Detection signals (verified on-device, not guessed):
 *  - YouTube Shorts: the "<pkg>:id/reel_recycler" view (also reel_watch_fragment_root)
 *  - Instagram Reels: "<pkg>:id/clips_tab" selected OR "<pkg>:id/like_button" visible
 *  - Facebook Reels: FbShortsViewerActivity / "View reels" description
 *  - TikTok / Snapchat: the whole app is short-form
 */
class RegainAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val main = Handler(Looper.getMainLooper())

    private var lastBlockPkg = ""
    private var lastBlockAt = 0L
    private var lastContentAt = 0L
    private var lastSiteScan = 0L
    @Volatile private var dismissedUntil = 0L

    /** Called when the user taps Go home / Go back on the overlay. */
    fun markDismissed() { dismissedUntil = System.currentTimeMillis() + 4000 }

    @Volatile private var foregroundOn = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        BlockNav.service = this
        BlockHolder.init(this)
        ShortFormHolder.init(this)
        ScheduleHolder.init(this)
        NotificationBlockHolder.init(this)
        syncProtectionState()
        startStateLoop()
    }

    /**
     * Keeps the service lightweight when no protection is configured: no foreground
     * notification and no frequent polling. Events still arrive via accessibility
     * callbacks; this loop only handles PiP/floating-window checks and state sync.
     */
    private fun startStateLoop() {
        scope.launch {
            while (isActive) {
                val protection = syncProtectionState()
                if (!protection) {
                    delay(15_000)
                    continue
                }
                if (ShortFormHolder.enabled.value && !BlockOverlayWindow.isShowing()) {
                    try { checkFloatingWindows() } catch (_: Exception) {}
                    delay(1500)
                } else {
                    delay(6_000)
                }
            }
        }
    }

    /** Ensure foreground state matches current protection config. Returns protection active. */
    private fun syncProtectionState(): Boolean {
        BlockHolder.init(this)
        ShortFormHolder.init(this)
        ScheduleHolder.init(this)
        val protection = anyProtection()
        if (protection) {
            if (!foregroundOn) startForegroundIfNeeded()
        } else {
            if (foregroundOn) stopForegroundCompat()
        }
        return protection
    }

    private fun checkFloatingWindows() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        for (w in windows) {
            val root = w.root ?: continue
            val pkg = root.packageName?.toString() ?: continue
            if (pkg == packageName || !isShortFormApp(pkg)) continue
            // The active Shorts feed is already handled by onAccessibilityEvent;
            // here we only need windows that are NOT the active tree.
            if (isShortFormWindow(pkg, root) && root != rootInActiveWindow) {
                block(pkg, "shorts", "floating window")
                return
            }
        }
    }

    /** Short-form signals inside a specific window's tree (used for PiP / floating). */
    private fun isShortFormWindow(pkg: String, root: AccessibilityNodeInfo): Boolean {
        if (isYouTube(pkg)) {
            if (findViewId(root, "$pkg:id/reel_recycler") != null) return true
            if (findViewId(root, "$pkg:id/reel_watch_fragment_root") != null) return true
        }
        if (isInstagramOrFacebook(pkg)) {
            if (findViewId(root, "$pkg:id/clips_tab")?.isSelected == true) return true
            if (findViewId(root, "$pkg:id/like_button")?.isVisibleToUser == true) return true
        }
        // This runs only for a short-form app's NON-active window, i.e. PiP /
        // floating video. Player controls there are enough to block.
        val text = collectText(root, 0, 80).lowercase()
        return text.contains("pause") || text.contains("play") || text.contains("next") ||
            text.contains("shorts") || text.contains("reel")
    }

    private fun isShortFormApp(pkg: String): Boolean =
        isYouTube(pkg) || isInstagramOrFacebook(pkg) ||
            pkg.contains("tiktok") || pkg.contains("musically") ||
            pkg.contains("trill") || pkg.contains("snapchat")

    private fun startForegroundIfNeeded() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(NotificationChannel("blocking", "App blocking", NotificationManager.IMPORTANCE_LOW))
            }
            val n = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, "blocking")
                    .setContentTitle("RegainPlus")
                    .setContentText("App blocking is active")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build()
            } else {
                Notification.Builder(this)
                    .setContentTitle("RegainPlus")
                    .setContentText("App blocking is active")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build()
            }
            startForeground(1001, n)
            foregroundOn = true
        } catch (_: Exception) {}
    }

    private fun stopForegroundCompat() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Exception) {}
        foregroundOn = false
    }

    private fun anyProtection(): Boolean =
        BlockHolder.blocked.value.isNotEmpty() || ShortFormHolder.enabled.value ||
            BlockHolder.sites.value.isNotEmpty() ||
            BlockHolder.isStudyModeActive() ||
            getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("adult_block", false)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!anyProtection()) return
        if (!foregroundOn) main.post { syncProtectionState() }
        val pkg = event.packageName?.toString() ?: return
        if (pkg.isBlank() || pkg == packageName) return

        // If the user switched to a different app while a block overlay was up,
        // dismiss it so it never gets stuck covering unrelated apps.
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            BlockOverlayWindow.isShowing() && !BlockOverlayWindow.isFor(pkg)) {
            main.post { BlockOverlayWindow.hide() }
        }

        // Content-change events fire constantly. Coalesce them.
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            val now = System.currentTimeMillis()
            if (now - lastContentAt < 250) return
            lastContentAt = now
        }

        scope.launch(Dispatchers.Default) { handleEvent(event, pkg) }
    }

    private fun handleEvent(event: AccessibilityEvent, pkg: String) {
        // 1. Whole-app blocking (user blocklist or schedule)
        if (BlockHolder.isBlocked(pkg) || ScheduleHolder.isBlockedNow(pkg)) {
            block(pkg, "app", "")
            return
        }

        // 2. Short-form blocking — this is the BlockerHero core
        if (ShortFormHolder.enabled.value && isShortForm(pkg, event)) {
            // Kick the user out of Shorts/Reels first, then cover the screen.
            block(pkg, "shorts", "")
            return
        }

        // 3. Browser: explicit blocklist + adult domains in the URL bar.
        //    This layer works even when Chrome uses secure DNS (DoH), which the
        //    DNS filter cannot see.
        if (isBrowser(pkg)) {
            val now = System.currentTimeMillis()
            if (now - lastSiteScan >= 1200) {
                lastSiteScan = now
                val reason = siteBlockReason(event.source)
                    ?: adultSiteReason(pkg, event)
                if (reason != null) block(pkg, "site", reason)
            }
        }
    }

    /** Detect adult domains from the browser's URL bar / visible address text. */
    private fun adultSiteReason(pkg: String, event: AccessibilityEvent): String? {
        if (!getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("adult_block", false)) return null
        val root = rootInActiveWindow
        val urlBar = findViewId(root, "$pkg:id/url_bar")
            ?: findViewId(root, "$pkg:id/url_bar_title")
        val text = if (urlBar != null) {
            "${urlBar.text ?: ""} ${urlBar.contentDescription ?: ""}"
        } else {
            collectText(event.source, 0, 60) + " " + collectText(root, 0, 100)
        }
        domainRegex.findAll(text.lowercase()).forEach { raw ->
            val domain = raw.value.removePrefix("www.").substringBefore("/")
            if (com.regainplus.util.DomainBlockUtil.isAdultDomain(domain)) return "adult content: $domain"
        }
        return null
    }

    /** Show the block screen. Once it is up for an app, keep it stable until dismissed. */
    private fun block(pkg: String, mode: String, reason: String) {
        // Do NOT recreate the overlay on every content-change event — that would
        // keep replacing the view and swallow the user's button taps.
        if (BlockOverlayWindow.isShowing() && BlockOverlayWindow.isFor(pkg)) return
        val now = System.currentTimeMillis()
        if (now < dismissedUntil) return
        if (pkg == lastBlockPkg && now - lastBlockAt < 1200) return
        lastBlockPkg = pkg
        lastBlockAt = now

        // For site blocks, leave the page; for short-form the overlay + Go home
        // is the escape — an automatic BACK can loop while the view stays in the tree.
        if (mode == "site") {
            try { performGlobalAction(GLOBAL_ACTION_BACK) } catch (_: Exception) {}
        }
        main.post {
            BlockOverlayWindow.show(this, pkg, mode, reason)
            if (!BlockOverlayWindow.isShowing()) {
                val i = Intent(this, BlockOverlayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    putExtra("pkg", pkg); putExtra("shorts", mode == "shorts"); putExtra("reason", reason)
                }
                try { startActivity(i) } catch (_: Exception) {}
            }
        }
    }

    // ---------- short-form detection ----------

    private val youtubePackages = listOf(
        "com.google.android.youtube", "com.vanced.android.youtube", "app.revanced.android.youtube"
    )
    private fun isYouTube(pkg: String) = youtubePackages.any { pkg == it || pkg.startsWith(it) }

    private fun isInstagramOrFacebook(pkg: String) =
        pkg == "com.instagram.android" || pkg == "com.instagram.lite" ||
            pkg == "com.facebook.katana" || pkg == "com.facebook.lite" ||
            pkg == "com.facebook.orca"

    /** True if this event/package represents short-form video. */
    private fun isShortForm(pkg: String, event: AccessibilityEvent): Boolean {
        val source = event.source

        if (isYouTube(pkg)) {
            // Primary signal: the Shorts feed recycler view (BlockerHero's exact check).
            if (findViewId(source, "$pkg:id/reel_recycler") != null) return true
            if (findViewId(rootInActiveWindow, "$pkg:id/reel_recycler") != null) return true
            // Secondary signals used by newer/older YouTube versions.
            if (findViewId(source, "$pkg:id/reel_watch_fragment_root") != null) return true
            if (findViewId(rootInActiveWindow, "$pkg:id/reel_watch_fragment_root") != null) return true
            val cls = event.className?.toString()?.lowercase() ?: ""
            if (cls.contains("shorts")) return true
            return false
        }

        if (isInstagramOrFacebook(pkg)) {
            // Reels viewer activity class.
            val cls = event.className?.toString() ?: ""
            if (cls.contains("FbShortsViewerActivity") || cls.lowercase().contains("reel")) return true
            // Selected Reels tab.
            if (findViewId(source, "$pkg:id/clips_tab")?.isSelected == true) return true
            if (findViewId(rootInActiveWindow, "$pkg:id/clips_tab")?.isSelected == true) return true
            // Like button visible => inside an open Reel.
            if (findViewId(source, "$pkg:id/like_button")?.isVisibleToUser == true) return true
            if (findViewId(rootInActiveWindow, "$pkg:id/like_button")?.isVisibleToUser == true) return true
            // Event description "View reels" / "Reels".
            val cd = event.contentDescription?.toString()?.lowercase() ?: ""
            if (cd.contains("reel")) return true
            // Last resort: visible text with reel player context.
            val text = collectText(rootInActiveWindow, 0, 120).lowercase()
            if (text.contains("reels") && (text.contains("next") || text.contains("like") || text.contains("comment"))) return true
            return false
        }

        // Whole short-form apps.
        if (pkg.contains("tiktok") || pkg.contains("musically") || pkg.contains("trill") ||
            pkg.contains("snapchat")) return true

        return false
    }

    /** Find a node by full view-id resource name (requires flagReportViewIds, which is set). */
    private fun findViewId(node: AccessibilityNodeInfo?, viewId: String): AccessibilityNodeInfo? {
        if (node == null) return null
        try {
            val found = node.findAccessibilityNodeInfosByViewId(viewId)
            if (!found.isNullOrEmpty()) return found[0]
        } catch (_: Exception) {}
        return null
    }

    // ---------- browser / domain blocking ----------

    private val browsers = listOf(
        "chrome", "firefox", "sbrowser", "emmx", "opera", "brave", "duckduckgo",
        "browser", "vivaldi", "miui.browser", "sec.android.app.sbrowser"
    )
    private fun isBrowser(pkg: String) = browsers.any { pkg.contains(it, ignoreCase = true) }

    private val domainRegex = Regex("(?:https?://)?(?:www\\.)?[a-z0-9-]+(?:\\.[a-z0-9-]+)+", RegexOption.IGNORE_CASE)

    private fun siteBlockReason(node: AccessibilityNodeInfo?): String? {
        if (BlockHolder.sites.value.isEmpty()) return null
        val text = collectText(node, 0, 140).lowercase()
        domainRegex.findAll(text).forEach { raw ->
            val domain = raw.value.removePrefix("www.").substringBefore("/")
            if (BlockHolder.isSiteBlocked(domain)) return "blocked site: $domain"
        }
        return null
    }

    /** Battery-safe, depth- and node-capped text traversal. */
    private fun collectText(node: AccessibilityNodeInfo?, depth: Int, remaining: Int): String {
        if (node == null || depth > 4 || remaining <= 0) return ""
        val sb = StringBuilder()
        var budget = remaining
        node.text?.let { sb.append(it).append(' ') }
        node.contentDescription?.let { sb.append(it).append(' ') }
        budget--
        for (i in 0 until node.childCount) {
            if (budget <= 0) break
            val child = node.getChild(i) ?: continue
            val s = collectText(child, depth + 1, budget)
            sb.append(s).append(' ')
            budget -= s.length / 8 + 1
        }
        return sb.toString()
    }

    override fun onInterrupt() {}
    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}
