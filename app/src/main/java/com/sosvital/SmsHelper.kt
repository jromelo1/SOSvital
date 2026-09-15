package com.sosvital

import android.content.Context
import android.location.Location
import android.os.Build
import android.telephony.SmsManager
import com.sosvital.model.EmergencyContact
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsHelper {

    fun sendEmergencySms(context: Context, contacts: List<EmergencyContact>, location: Location?) {
        if (contacts.isEmpty()) return

        val msg = buildMessage(
            userName = ContactsManager.getUserName(context),
            locationUrl = LocationHelper.toMapsUrl(location),
            time = SimpleDateFormat("HH:mm  dd/MM/yyyy", Locale.getDefault()).format(Date())
        )

        val sms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            context.getSystemService(SmsManager::class.java)
        else
            @Suppress("DEPRECATION") SmsManager.getDefault()

        contacts.forEach { contact ->
            runCatching {
                sms.sendMultipartTextMessage(
                    contact.phone, null, sms.divideMessage(msg), null, null
                )
            }
        }
    }

    private fun buildMessage(userName: String, locationUrl: String, time: String) = """
⚠️ EMERGENCIA SISMICA
$userName no respondio a la alerta sismica.
Ubicacion: $locationUrl
Hora: $time
[Enviado automaticamente por SOSvital]
    """.trimIndent()
}
