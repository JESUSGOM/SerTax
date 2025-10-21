package com.sertax.api.service

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

    /**
     * Notifica a un conductor específico sobre una nueva oferta de viaje.
     * El frontend del conductor debe estar suscrito a /topic/driver/{driverId}
     */
    fun notifyDriverOfNewTrip(driverId: Long, trip: Trip) {
        val destination = "/topic/driver/$driverId"
        val payload = mapOf(
            "type" to "NEW_TRIP_OFFER",
            "tripId" to trip.tripId,
            "pickupAddress" to trip.pickupAddress,
            "destinationAddress" to trip.destinationAddress
        )
        messagingTemplate.convertAndSend(destination, payload)
        LOGGER.info("Notificando al conductor $driverId sobre el viaje ${trip.tripId} en el topic $destination")
    }

    /**
     * Notifica a un usuario sobre cualquier actualización en su viaje.
     * El frontend del usuario debe estar suscrito a /topic/user/{userId}
     */
    fun notifyUserOfTripUpdate(userId: Long, trip: Trip) {
        val destination = "/topic/user/$userId"
        val payload = mapOf(
            "type" to "TRIP_STATUS_UPDATE",
            "tripId" to trip.tripId,
            "newStatus" to trip.status.name,
            "driverName" to (trip.driver?.name ?: ""),
            "vehicleModel" to (trip.driver?.license?.let { lic -> lic.association?.name ?: "" } ?: "") // Ejemplo de dato adicional
        )
        messagingTemplate.convertAndSend(destination, payload)
        LOGGER.info("Notificando al usuario $userId sobre el viaje ${trip.tripId} en el topic $destination")
    }
}