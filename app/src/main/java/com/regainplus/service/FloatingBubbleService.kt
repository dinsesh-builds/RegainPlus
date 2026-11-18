package com.regainplus.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout

class FloatingBubbleService : Service() {
    private var windowManager: WindowManager? = null
    private var bubbleView: LinearLayout? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            stopSelf(); return
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        bubbleView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val btn = Button(this@FloatingBubbleService).apply {
                text = "Focus"
                setOnClickListener {
                    // BUG-09 fix: actually open the Block screen
                    val i = Intent(this@FloatingBubbleService, com.regainplus.MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("openBlock", true)
                    }
                    startActivity(i)
                }
            }
            addView(btn)
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 100; y = 200 }
        try { windowManager?.addView(bubbleView, params) } catch (_: Exception) { stopSelf() }
    }
    override fun onDestroy() {
        super.onDestroy()
        try { bubbleView?.let { windowManager?.removeView(it) } } catch (_: Exception) {}
    }
}
