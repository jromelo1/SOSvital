package com.sosvital

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sosvital.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ContactsAdapter

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refreshStatusBadges() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupContactsList()
        setupButtons()
        requestRuntimePermissions()
    }

    override fun onResume() {
        super.onResume()
        refreshContactsList()
        refreshStatusBadges()
    }

    // -------------------------------------------------------------------------
    // Contacts
    // -------------------------------------------------------------------------

    private fun setupContactsList() {
        adapter = ContactsAdapter { contact ->
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(getString(R.string.confirm_delete_msg, contact.name))
                .setPositiveButton(R.string.delete) { _, _ ->
                    ContactsManager.removeContact(this, contact.id)
                    refreshContactsList()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        binding.rvContacts.adapter = adapter
    }

    private fun refreshContactsList() {
        val contacts = ContactsManager.getContacts(this)
        adapter.submitList(contacts)
        binding.tvNoContacts.visibility = if (contacts.isEmpty())
            android.view.View.VISIBLE else android.view.View.GONE
    }

    // -------------------------------------------------------------------------
    // Buttons
    // -------------------------------------------------------------------------

    private fun setupButtons() {
        binding.fabAddContact.setOnClickListener {
            startActivity(Intent(this, AddContactActivity::class.java))
        }

        binding.btnNotificationAccess.setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        binding.btnBatteryOptimization.setOnClickListener {
            requestBatteryOptimizationExemption()
        }

        binding.btnSaveUserName.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            if (name.isNotEmpty()) {
                ContactsManager.setUserName(this, name)
                Toast.makeText(this, R.string.name_saved, Toast.LENGTH_SHORT).show()
            }
        }

        // Test button — simulates the full alert flow without a real earthquake
        binding.btnTestAlert.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.test_alert_title)
                .setMessage(R.string.test_alert_msg)
                .setPositiveButton(R.string.simulate) { _, _ ->
                    startActivity(Intent(this, AlertActivity::class.java))
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    // -------------------------------------------------------------------------
    // Status indicators
    // -------------------------------------------------------------------------

    private fun refreshStatusBadges() {
        val notifOk = isNotificationListenerEnabled()
        val locationOk = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val smsOk = hasPermission(Manifest.permission.SEND_SMS)

        binding.tvStatusNotif.apply {
            text = if (notifOk) "✅ Acceso a notificaciones" else "❌ Acceso a notificaciones"
            setTextColor(if (notifOk) 0xFF2E7D32.toInt() else 0xFFD32F2F.toInt())
        }
        binding.tvStatusLocation.apply {
            text = if (locationOk) "✅ Ubicación" else "❌ Ubicación"
            setTextColor(if (locationOk) 0xFF2E7D32.toInt() else 0xFFD32F2F.toInt())
        }
        binding.tvStatusSms.apply {
            text = if (smsOk) "✅ SMS" else "❌ SMS"
            setTextColor(if (smsOk) 0xFF2E7D32.toInt() else 0xFFD32F2F.toInt())
        }

        binding.etUserName.setText(ContactsManager.getUserName(this))
    }

    // -------------------------------------------------------------------------
    // Permissions
    // -------------------------------------------------------------------------

    private fun requestRuntimePermissions() {
        val needed = mutableListOf<String>()
        if (!hasPermission(Manifest.permission.SEND_SMS)) needed += Manifest.permission.SEND_SMS
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            needed += Manifest.permission.ACCESS_FINE_LOCATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasPermission(Manifest.permission.POST_NOTIFICATIONS))
            needed += Manifest.permission.POST_NOTIFICATIONS

        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    private fun requestBatteryOptimizationExemption() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        )
        return enabled?.contains(packageName) == true
    }

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
