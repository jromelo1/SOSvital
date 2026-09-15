package com.sosvital

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Listens to all system notifications and triggers the SOS alert when earthquake
 * keywords are detected. The user must grant Notification Access in Settings.
 *
 * Google's built-in earthquake alerts arrive via com.google.android.gms or
 * the cell-broadcast receiver — both are covered by TRUSTED_PACKAGES below.
 */
class EarthquakeNotificationListener : NotificationListenerService() {

    companion object {
        private val EARTHQUAKE_KEYWORDS = setOf(
            "earthquake", "terremoto", "sismo", "seismo",
            "alerta sismica", "alerta sísmica", "earthquake alert",
            "tremor", "temblor", "shake alert", "sasmex", "early warning"
        )

        // Packages that legitimately send seismic alerts on Android
        private val TRUSTED_PACKAGES = setOf(
            "com.google.android.gms",
            "com.android.cellbroadcastreceiver",
            "com.android.cellbroadcastservice",
            "com.google.android.apps.safetycenter"
        )

        const val CHANNEL_ID = "sosvital_seismic_alert"
        private const val NOTIF_ID = 9001
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (isEarthquakeAlert(sbn)) triggerAlert()
    }

    private fun isEarthquakeAlert(sbn: StatusBarNotification): Boolean {
        val pkg = sbn.packageName.lowercase()
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE, "").lowercase()
        val body = extras.getString(Notification.EXTRA_TEXT, "").lowercase()
        val combined = "$title $body"

        val fromTrusted = TRUSTED_PACKAGES.any { pkg.contains(it) }
        val hasKeyword = EARTHQUAKE_KEYWORDS.any { combined.contains(it) }

        // High-confidence keywords are accepted regardless of package
        val highConfidence = combined.contains("sismo") ||
            combined.contains("terremoto") ||
            combined.contains("earthquake alert")

        return (fromTrusted && hasKeyword) || highConfidence
    }

    private fun triggerAlert() {
        ensureChannel()

        val intent = Intent(this, AlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ ALERTA SÍSMICA DETECTADA")
            .setContentText("Toca para confirmar que estás bien")
            .setPriority(Notification.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_ALARM)
            .setFullScreenIntent(pi, true)   // wakes screen, shows over lock screen
            .setOngoing(true)
            .build()

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIF_ID, notification)
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alertas Sísmicas SOSvital",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }
}
