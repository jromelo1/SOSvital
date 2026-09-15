package com.sosvital.model

import java.util.UUID

data class EmergencyContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String
)
