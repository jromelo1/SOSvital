package com.sosvital

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sosvital.model.EmergencyContact

object ContactsManager {

    private const val PREFS_NAME = "sosvital_prefs"
    private const val KEY_CONTACTS = "emergency_contacts"
    private const val KEY_USER_NAME = "user_name"
    private val gson = Gson()

    fun getContacts(context: Context): List<EmergencyContact> {
        val json = prefs(context).getString(KEY_CONTACTS, null) ?: return emptyList()
        val type = object : TypeToken<List<EmergencyContact>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveContacts(context: Context, contacts: List<EmergencyContact>) {
        prefs(context).edit().putString(KEY_CONTACTS, gson.toJson(contacts)).apply()
    }

    fun addContact(context: Context, contact: EmergencyContact) {
        saveContacts(context, getContacts(context) + contact)
    }

    fun removeContact(context: Context, contactId: String) {
        saveContacts(context, getContacts(context).filter { it.id != contactId })
    }

    fun getUserName(context: Context): String =
        prefs(context).getString(KEY_USER_NAME, "Usuario SOSvital") ?: "Usuario SOSvital"

    fun setUserName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_USER_NAME, name).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
