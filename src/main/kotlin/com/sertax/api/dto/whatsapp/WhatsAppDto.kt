package com.sertax.api.dto.whatsapp

import com.fasterxml.jackson.annotation.JsonProperty

// Estructura principal del payload del webhook de WhatsApp
data class WhatsAppWebhookRequest(
    val `object`: String,
    val entry: List<WhatsAppEntry>
)

data class WhatsAppEntry(
    val id: String,
    val changes: List<WhatsAppChange>
)

data class WhatsAppChange(
    val field: String,
    val value: WhatsAppValue
)

data class WhatsAppValue(
    val messaging_product: String,
    val metadata: WhatsAppMetadata,
    val contacts: List<WhatsAppContact>,
    val messages: List<WhatsAppMessage>?
)

data class WhatsAppMetadata(
    val display_phone_number: String,
    val phone_number_id: String
)

data class WhatsAppContact(
    val profile: WhatsAppProfile,
    @JsonProperty("wa_id") val waId: String
)

data class WhatsAppProfile(
    val name: String
)

data class WhatsAppMessage(
    val from: String, // Número de teléfono del usuario
    val id: String,
    val timestamp: String,
    val type: String, // "text", "location", "interactive", etc.
    val text: WhatsAppText?,
    val location: WhatsAppLocation?
)

data class WhatsAppText(
    val body: String
)

data class WhatsAppLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String?,
    val address: String?
)