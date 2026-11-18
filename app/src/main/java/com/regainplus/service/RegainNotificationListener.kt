package com.regainplus.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** Blocks notifications from blocked apps and stores them in an on-device inbox. */
class RegainNotificationListener : NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
        BlockHolder.init(this)
        NotificationBlockHolder.init(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        BlockHolder.init(this)
        NotificationBlockHolder.init(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val n = sbn ?: return
        val pkg = n.packageName ?: return
        if (pkg == packageName) return
        if (!NotificationBlockHolder.isNotificationBlocked(pkg)) return
        val extras = n.notification.extras
        val title = (extras.getCharSequence(Notification.EXTRA_TITLE)
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG) ?: "").toString()
        val text = (extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: "").toString()
        NotificationBlockHolder.addBlocked(this, pkg, title, text)
        cancelNotification(n.key)
    }
}
