package com.sertax.api.service

import com.sertax.api.model.Driver
import com.sertax.api.model.DriverRealtimeStatus
import com.sertax.api.model.Trip
import com.sertax.api.model.TripStatus
import com.sertax.api.repository.TripRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.logging.Logger

@Service
class AssignmentService(
    private val geolocationService: GeolocationService,
    private val tripRepository: TripRepository,
    private val notificationService: NotificationService // <-- INYECTADO
) {
    companion object {
        private val LOGGER = Logger.getLogger(AssignmentService::class.java.name)
    }

    @Transactional
    fun findAndAssignDriverForTrip(trip: Trip, needsPMR: Boolean, withPet: Boolean) {
        LOGGER.info("Iniciando búsqueda de conductor para viaje ${trip.tripId} con filtros PMR=$needsPMR, Pet=$withPet")

        val candidates = findCandidates(trip, needsPMR, withPet)

        if (candidates.isNotEmpty()) {
            val driverToNotify = candidates.first()

            // Asignamos el conductor al viaje para reservarlo y cambiamos el estado
            trip.driver = driverToNotify
            trip.status = TripStatus.Assigned
            tripRepository.save(trip)

            // Enviamos la notificación al conductor vía WebSocket
            notificationService.notifyDriverOfNewTrip(driverToNotify.driverId, trip)

            LOGGER.info("Viaje ${trip.tripId} asignado al conductor ${driverToNotify.driverId}. Esperando aceptación.")
        } else {
            handleNoDriverFound(trip)
        }
    }

    private fun findCandidates(trip: Trip, needsPMR: Boolean, withPet: Boolean): List<Driver> {
        val candidatesAtStop = geolocationService.findAvailableDriversNearby(
            trip.pickupLocation, 1000.0, DriverRealtimeStatus.AtStop, needsPMR, withPet
        )
        if (candidatesAtStop.isNotEmpty()) return candidatesAtStop

        return geolocationService.findAvailableDriversNearby(
            trip.pickupLocation, 1500.0, DriverRealtimeStatus.Free, needsPMR, withPet
        )
    }

    private fun handleNoDriverFound(trip: Trip) {
        trip.status = TripStatus.Cancelled
        val savedTrip = tripRepository.save(trip)
        // Notificamos al usuario que no se encontró conductor
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)
        LOGGER.warning("No se encontraron conductores para el viaje ${trip.tripId}.")
    }
}