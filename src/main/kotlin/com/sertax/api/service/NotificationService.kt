package com.sertax.api.service

import com.sertax.api.dto.chat.OutboundChatMessageDto
import com.sertax.api.model.Message
import com.sertax.api.model.SenderType
import com.sertax.api.model.Trip
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class NotificationService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    companion object {
        private val LOGGER = Logger.getLogger(NotificationService::class.java.name)
    }

    fun notifyDriverOfNewTrip(driverId: Long, trip: Trip) {
        val destination = "/topic/driver/$driverId"
        val payload = mapOf("type" to "NEW_TRIP_OFFER", "trip" to trip)
        messagingTemplate.convertAndSend(destination, payload)
    }

    fun notifyUserOfTripUpdate(userId: Long, trip: Trip) {
        val destination = "/topic/user/$userId"
        val payload = mapOf("type" to "TRIP_STATUS_UPDATE", "trip" to trip)
        messagingTemplate.convertAndSend(destination, payload)
    }

    fun notifyDriverOfCancellation(driverId: Long, tripId: Long) {
        val destination = "/topic/driver/$driverId"
        val payload = mapOf("type" to "TRIP_CANCELLED_BY_USER", "tripId" to tripId)
        messagingTemplate.convertAndSend(destination, payload)
    }

    fun notifyUserOfDriverCancellation(userId: Long, trip: Trip) {
        val destination = "/topic/user/$userId"
        val payload = mapOf("type" to "TRIP_CANCELLED_BY_DRIVER", "message" to "Tu conductor ha cancelado. Estamos buscando un nuevo taxi para ti.")
        messagingTemplate.convertAndSend(destination, payload)
    }

    fun notifyUserOfDriverRejection(userId: Long, trip: Trip) {
        val destination = "/topic/user/$userId"
        val payload = mapOf("type" to "TRIP_REJECTED_BY_DRIVER", "message" to "El conductor no pudo aceptar tu viaje. ¡Estamos buscando a otro!")
        messagingTemplate.convertAndSend(destination, payload)
    }

    /**
     * Reenvía un mensaje de chat guardado al usuario y al conductor del viaje.
     */
    fun sendChatMessage(message: Message) {
        val destination = "/topic/trip/${message.trip.tripId}" // Canal de chat específico del viaje
        val outboundMessage = OutboundChatMessageDto(
            messageId = message.messageId,
            tripId = message.trip.tripId,
            senderId = message.senderId,
            senderType = message.senderType,
            content = message.content,
            timestamp = message.timestamp
        )
        messagingTemplate.convertAndSend(destination, outboundMessage)
        LOGGER.info("Mensaje ${message.messageId} enviado al topic de viaje ${message.trip.tripId}")
    }

    /**
     * Notifica al usuario que su viaje ha sido cancelado por no presentarse.
     */
    fun notifyUserOfNoShow(userId: Long, trip: Trip) {
        val destination = "/topic/user/$userId"
        val payload = mapOf(
            "type" to "TRIP_CANCELLED_NO_SHOW",
            "tripId" to trip.tripId,
            "message" to "Tu viaje ha sido cancelado porque el conductor no pudo encontrarte en el punto de recogida."
        )
        messagingTemplate.convertAndSend(destination, payload)
        LOGGER.info("Notificando al usuario $userId sobre no comparecencia para el viaje ${trip.tripId}")
    }
}