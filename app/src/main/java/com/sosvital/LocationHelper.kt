package com.sosvital

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object LocationHelper {

    fun getCurrentLocation(context: Context, callback: (Location?) -> Unit) {
        if (!hasPermission(context)) {
            callback(null)
            return
        }
        val client = LocationServices.getFusedLocationProviderClient(context)
        // Last known location is fastest — ideal for an emergency
        client.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    callback(loc)
                } else {
                    // No cached fix — request a fresh one (up to 10s)
                    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { callback(it) }
                        .addOnFailureListener { callback(null) }
                }
            }
            .addOnFailureListener { callback(null) }
    }

    fun toMapsUrl(location: Location?): String =
        if (location != null)
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        else
            "ubicacion no disponible"

    private fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
