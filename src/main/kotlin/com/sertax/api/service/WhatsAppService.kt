package com.sertax.api.service

import com.sertax.api.dto.trip.CreateTripRequest
import com.sertax.api.dto.whatsapp.WhatsAppWebhookRequest
import com.sertax.api.model.RequestChannel
import com.sertax.api.model.User
import com.sertax.api.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.concurrent.ConcurrentHashMap

enum class ConversationState {
    AWAITING_LOCATION, AWAITING_CONFIRMATION
}

@Service
class WhatsAppService(
    private val userRepository: UserRepository,
    private val tripService: TripService
) {
    private val restTemplate = RestTemplate()
    private val userStates = ConcurrentHashMap<String, ConversationState>()
    private val userTripData = ConcurrentHashMap<String, CreateTripRequest>()

    @Value("\${whatsapp.api.token}")
    private lateinit var apiToken: String

    @Value("\${whatsapp.phone.number.id}")
    private lateinit var phoneNumberId: String

    fun processIncomingMessage(payload: WhatsAppWebhookRequest) {
        val message = payload.entry.firstOrNull()?.changes?.firstOrNull()?.value?.messages?.firstOrNull() ?: return
        val userPhone = message.from
        val userName = payload.entry.firstOrNull()?.changes?.firstOrNull()?.value?.contacts?.firstOrNull()?.profile?.name ?: "Usuario"
        val state = userStates[userPhone]

        if (state == null || message.text?.body?.equals("hola", ignoreCase = true) == true) {
            // Iniciar o reiniciar conversación
            sendMessage(userPhone, "¡Hola $userName! Bienvenido al servicio de taxi de Santa Cruz. Por favor, comparte tu ubicación para solicitar un taxi.")
            userStates[userPhone] = ConversationState.AWAITING_LOCATION
        } else {
            when (state) {
                ConversationState.AWAITING_LOCATION -> {
                    if (message.location != null) {
                        // El usuario envió la ubicación
                        val user = findOrCreateUser(userPhone, userName)
                        val tripRequest = CreateTripRequest(
                            userId = user.userId,
                            pickupAddress = message.location.address ?: "Ubicación compartida",
                            pickupLatitude = message.location.latitude,
                            pickupLongitude = message.location.longitude,
                            destinationAddress = null,
                            destinationLatitude = null,
                            destinationLongitude = null
                        )

                        tripService.requestTrip(tripRequest)
                        sendMessage(userPhone, "¡Gracias! Ya estamos buscando un taxi para ti en ${tripRequest.pickupAddress}. Te notificaremos cuando sea asignado.")
                        userStates.remove(userPhone) // Fin de la conversación
                    } else {
                        sendMessage(userPhone, "No he entendido tu mensaje. Por favor, usa el botón de adjuntar para compartir tu ubicación.")
                    }
                }
                // Otros estados de conversación irían aquí
                else -> {}
            }
        }
    }

    private fun findOrCreateUser(phone: String, name: String): User {
        var user = userRepository.findByPhoneNumber(phone)
        if (user == null) {
            user = User(
                name = name,
                email = "$phone@whatsapp.sertax", // Email de marcador de posición
                phoneNumber = phone,
                passwordHash = "whatsapp_user_no_password", // No se usará para login
                isActive = true
            )
            userRepository.save(user)
        }
        return user
    }

    private fun sendMessage(to: String, text: String) {
        val url = "https://graph.facebook.com/v19.0/$phoneNumberId/messages"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer $apiToken")

        val body = mapOf(
            "messaging_product" to "whatsapp",
            "to" to to,
            "type" to "text",
            "text" to mapOf("body" to text)
        )
        val request = HttpEntity(body, headers)
        try {
            restTemplate.postForEntity(url, request, String::class.java)
        } catch (e: Exception) {
            // Manejar errores de envío
            println("Error al enviar mensaje de WhatsApp: ${e.message}")
        }
    }
}