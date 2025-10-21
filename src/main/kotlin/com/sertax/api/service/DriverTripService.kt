package com.sertax.api.service

import com.sertax.api.model.Trip
import com.sertax.api.model.TripStatus
import com.sertax.api.repository.TripRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class DriverTripService(
    private val tripRepository: TripRepository,
    private val notificationService: NotificationService // <-- INYECTADO
) {

    private fun findTripAndValidateDriver(tripId: Long, driverId: Long): Trip {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Viaje con ID $tripId no encontrado.") }

        if (trip.driver?.driverId != driverId) {
            throw SecurityException("No tienes permiso para gestionar este viaje.")
        }
        return trip
    }

    @Transactional
    fun acceptTrip(tripId: Long, driverId: Long): Trip {
        val trip = findTripAndValidateDriver(tripId, driverId)

        if (trip.status != TripStatus.Assigned) {
            throw IllegalStateException("Este viaje no puede ser aceptado.")
        }

        trip.status = TripStatus.EnRoute
        val savedTrip = tripRepository.save(trip)

        // Notificamos al usuario que el conductor ha aceptado y está en camino
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)

        return savedTrip
    }

    @Transactional
    fun startTrip(tripId: Long, driverId: Long): Trip {
        val trip = findTripAndValidateDriver(tripId, driverId)

        if (trip.status != TripStatus.EnRoute) {
            throw IllegalStateException("El conductor debe estar en ruta para iniciar el viaje.")
        }

        trip.status = TripStatus.InProgress
        trip.pickupTimestamp = OffsetDateTime.now()
        val savedTrip = tripRepository.save(trip)

        // Notificamos al usuario que el viaje ha comenzado
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)

        return savedTrip
    }

    @Transactional
    fun completeTrip(tripId: Long, driverId: Long, finalCost: BigDecimal): Trip {
        val trip = findTripAndValidateDriver(tripId, driverId)

        if (trip.status != TripStatus.InProgress) {
            throw IllegalStateException("No se puede completar un viaje que no está en curso.")
        }

        trip.status = TripStatus.Completed
        trip.finalCost = finalCost
        trip.completionTimestamp = OffsetDateTime.now()
        val savedTrip = tripRepository.save(trip)

        // Notificamos al usuario que el viaje ha finalizado
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)

        return savedTrip
    }
}