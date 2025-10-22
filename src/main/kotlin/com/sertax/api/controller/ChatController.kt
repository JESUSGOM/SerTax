package com.sertax.api.controller

import com.sertax.api.dto.chat.InboundChatMessageDto
import com.sertax.api.model.SenderType
import com.sertax.api.repository.UserRepository
import com.sertax.api.service.ChatService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ChatController(
    private val chatService: ChatService,
    private val userRepository: UserRepository
    // En el futuro, se inyectaría el DriverRepository para la autenticación de conductores
) {

    /**
     * Recibe los mensajes enviados por los clientes al destino "/app/chat.send".
     * Principal es el usuario autenticado (gracias a la seguridad de Spring WebSocket).
     */
    @MessageMapping("/chat.send")
    fun sendMessage(@Payload chatMessage: InboundChatMessageDto, principal: Principal) {
        // Identificamos si el remitente es un Usuario o un Conductor
        // El 'principal.name' suele ser el email o username del usuario autenticado
        val user = userRepository.findByEmail(principal.name)
        if (user != null) {
            // Si encontramos un usuario, el remitente es el usuario
            chatService.processAndSendMessage(user.userId, SenderType.User, chatMessage)
        } else {
            // Si no es un usuario, asumimos que es un conductor.
            // La lógica para obtener el ID del conductor desde el 'principal' dependerá
            // de cómo se implemente la autenticación de conductores en Spring Security.
            // Por ahora, lanzamos una excepción si el remitente no es un usuario reconocido.
            // val driver = driverRepository.findByUsername(principal.name)
            // if(driver != null) {
            //     chatService.processAndSendMessage(driver.driverId, SenderType.Driver, chatMessage)
            // } else {
            //     throw SecurityException("Remitente no autenticado o no encontrado.")
            // }
        }
    }
}