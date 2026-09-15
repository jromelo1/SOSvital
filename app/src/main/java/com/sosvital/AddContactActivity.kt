package com.sosvital

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sosvital.databinding.ActivityAddContactBinding
import com.sosvital.model.EmergencyContact

class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.add_contact_title)

        binding.btnSave.setOnClickListener { saveContact() }
    }

    private fun saveContact() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (!phone.matches(Regex("^\\+?[0-9\\s\\-]{7,15}\$"))) {
            Toast.makeText(this, R.string.invalid_phone, Toast.LENGTH_SHORT).show()
            return
        }

        ContactsManager.addContact(this, EmergencyContact(name = name, phone = phone))
        Toast.makeText(this, R.string.contact_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
