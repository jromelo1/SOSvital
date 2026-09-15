package com.sosvital

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-enables the NotificationListenerService after reboot so the earthquake
 * detector survives device restarts without user interaction.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // NotificationListenerService is managed by the system; toggling the
            // component forces Android to re-evaluate and reconnect it.
            val component = android.content.ComponentName(
                context, EarthquakeNotificationListener::class.java
            )
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                component,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                component,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
        }
    }
}
