package com.sertax.api.service

import com.sertax.api.dto.chat.InboundChatMessageDto
import com.sertax.api.model.Message
import com.sertax.api.model.SenderType
import com.sertax.api.model.TripStatus
import com.sertax.api.repository.MessageRepository
import com.sertax.api.repository.TripRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val messageRepository: MessageRepository,
    private val tripRepository: TripRepository,
    private val notificationService: NotificationService
) {

    @Transactional
    fun processAndSendMessage(senderId: Long, senderType: SenderType, messageDto: InboundChatMessageDto) {
        val trip = tripRepository.findByIdOrNull(messageDto.tripId)
            ?: throw NoSuchElementException("Viaje con ID ${messageDto.tripId} no encontrado.")

        // Validación: Solo se puede chatear en viajes activos
        if (trip.status !in listOf(TripStatus.Assigned, TripStatus.EnRoute, TripStatus.InProgress)) {
            throw IllegalStateException("No se puede enviar mensajes en un viaje que no está activo.")
        }

        // Validación: El remitente debe ser parte del viaje
        val isUser = senderType == SenderType.User && trip.user.userId == senderId
        val isDriver = senderType == SenderType.Driver && trip.driver?.driverId == senderId
        if (!isUser && !isDriver) {
            throw SecurityException("No tienes permiso para enviar mensajes en este viaje.")
        }

        // Guardar el mensaje en la base de datos
        val message = Message(
            trip = trip,
            senderId = senderId,
            senderType = senderType,
            content = messageDto.content
        )
        val savedMessage = messageRepository.save(message)

        // Enviar el mensaje a través de WebSocket a la otra parte
        notificationService.sendChatMessage(savedMessage)
    }
}