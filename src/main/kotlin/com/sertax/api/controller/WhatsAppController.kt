package com.sertax.api.controller

import com.sertax.api.dto.whatsapp.WhatsAppWebhookRequest
import com.sertax.api.service.WhatsAppService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

@RestController
@RequestMapping("/api/whatsapp")
class WhatsAppController(private val whatsAppService: WhatsAppService) {

    @Value("\${whatsapp.verify.token}")
    private lateinit var verifyToken: String

    companion object {
        private val LOGGER = Logger.getLogger(WhatsAppController::class.java.name)
    }

    /**
     * Endpoint para la verificación del Webhook por parte de Meta.
     * Se usa una sola vez durante la configuración.
     */
    @GetMapping("/webhook")
    fun verifyWebhook(
        @RequestParam("hub.mode") mode: String?,
        @RequestParam("hub.verify_token") token: String?,
        @RequestParam("hub.challenge") challenge: String?
    ): ResponseEntity<String> {
        if ("subscribe" == mode && verifyToken == token) {
            LOGGER.info("Webhook verificado correctamente.")
            return ResponseEntity.ok(challenge)
        } else {
            LOGGER.warning("Fallo en la verificación del Webhook.")
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    /**
     * Endpoint que recibe las notificaciones de mensajes entrantes de los usuarios.
     */
    @PostMapping("/webhook")
    fun handleWebhook(@RequestBody payload: WhatsAppWebhookRequest): ResponseEntity<Void> {
        try {
            // TODO: En producción, verificar la firma de la petición (X-Hub-Signature-256)
            whatsAppService.processIncomingMessage(payload)
        } catch (e: Exception) {
            LOGGER.severe("Error procesando el payload de WhatsApp: ${e.message}")
        }
        return ResponseEntity.ok().build()
    }
}