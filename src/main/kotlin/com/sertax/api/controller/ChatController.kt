package com.sertax.api.controller

import com.sertax.api.dto.chat.InboundChatMessageDto
import com.sertax.api.model.SenderType
import com.sertax.api.repository.DriverRepository
import com.sertax.api.repository.UserRepository
import com.sertax.api.service.ChatService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ChatController(
    private val chatService: ChatService,
    private val userRepository: UserRepository,
    private val driverRepository: DriverRepository
) {

    /**
     * Recibe los mensajes enviados por los clientes al destino "/app/chat.send".
     * Principal es el usuario autenticado (gracias a la seguridad de Spring).
     */
    @MessageMapping("/chat.send")
    fun sendMessage(@Payload chatMessage: InboundChatMessageDto, principal: Principal) {
        // Identificamos si el remitente es un Usuario o un Conductor
        // En una implementación real, el 'principal' de Spring Security contendría esta información.
        // Aquí simulamos la búsqueda para determinar el tipo y el ID.
        val user = userRepository.findByEmail(principal.name)
        if (user != null) {
            chatService.processAndSendMessage(user.userId, SenderType.User, chatMessage)
        } else {
            // Si no es un usuario, podría ser un conductor. La lógica de autenticación del conductor iría aquí.
            // val driver = driverRepository.findByUsername(principal.name)
            // if (driver != null) {
            //     chatService.processAndSendMessage(driver.driverId, SenderType.Driver, chatMessage)
            // }
        }
    }
}