package com.regainplus.service

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Block screen drawn as a system overlay — with reason + context-aware escape. */
object BlockOverlayWindow {
    @Volatile private var view: View? = null
    private var wm: WindowManager? = null
    @Volatile private var blockedPkg: String? = null

    fun isShowing() = view != null
    fun isFor(pkg: String) = blockedPkg == pkg
    fun canDraw(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(ctx)

    /**
     * @param mode "shorts" | "site" | "app"
     * @param reason what triggered it (keyword/domain), shown to the user
     */
    fun show(ctx: Context, pkg: String, mode: String, reason: String = "") {
        if (!canDraw(ctx)) return
        hide()
        val manager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val label = try {
            pmLabel(ctx, pkg)
        } catch (_: Exception) { pkg }

        val title = when (mode) {
            "shorts" -> "Short-form blocked"
            "site" -> "Content blocked"
            else -> "App blocked"
        }
        val body = when (mode) {
            "shorts" -> "Short-form content hijacks attention. You opened $label."
            "site" -> "Blocked because: $reason\n\nGo back to leave this page."
            else -> "$label is on your blocklist."
        }
        val actionLabel = if (mode == "site") "Go back" else "Go home"

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(17, 20, 18))
            // Consume background touches without swallowing the Button's clicks.
            // (A root setOnTouchListener returning true would block all children.)
            isClickable = true
            addView(TextView(ctx).apply {
                text = title; textSize = 26f; setTextColor(Color.rgb(242,245,240)); gravity = Gravity.CENTER
            })
            addView(TextView(ctx).apply {
                text = body; textSize = 16f; setTextColor(Color.rgb(154,163,152)); gravity = Gravity.CENTER
                setPadding(48, 32, 48, 40)
            })
            addView(Button(ctx).apply {
                text = actionLabel
                setOnClickListener {
                    hide()
                    if (mode == "site") {
                        // Leave the page but stay in the browser (MIUI-safe via service)
                        BlockNav.markDismissed(); BlockNav.goBack()
                    } else {
                        // AccessibilityService global action — reliable on MIUI where
                        // background Activity launches are denied.
                        BlockNav.markDismissed(); BlockNav.goHome()
                    }
                }
            })
        }
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            Color.TRANSPARENT
        ).apply { gravity = Gravity.CENTER }
        try { manager.addView(root, params); view = root; wm = manager; blockedPkg = pkg } catch (_: Exception) {}
    }

    private fun pmLabel(ctx: Context, pkg: String): String =
        try {
            val appInfo = ctx.packageManager.getApplicationInfo(pkg, 0)
            ctx.packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) { pkg }

    fun hide() {
        try { view?.let { wm?.removeView(it) } } catch (_: Exception) {}
        view = null; wm = null; blockedPkg = null
    }
}

/** Holds the accessibility service instance so the overlay can trigger Back/Home safely. */
object BlockNav {
    @Volatile var service: RegainAccessibilityService? = null
    fun markDismissed() { service?.markDismissed() }
    fun goBack() {
        service?.let {
            try { it.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK) } catch (_: Exception) {}
        }
    }
    fun goHome() {
        service?.let {
            try { it.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME) } catch (_: Exception) {}
        }
    }
}
