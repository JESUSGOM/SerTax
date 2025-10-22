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
    private val notificationService: NotificationService
) {
    companion object {
        private val LOGGER = Logger.getLogger(AssignmentService::class.java.name)
    }

    @Transactional
    fun findAndAssignDriverForTrip(
        trip: Trip,
        needsPMR: Boolean,
        withPet: Boolean,
        excludeDriverIds: List<Long> = emptyList() // <-- AÑADIDO: Parámetro de exclusión
    ) {
        LOGGER.info("Iniciando búsqueda para viaje ${trip.tripId}, excluyendo conductores: $excludeDriverIds")

        val candidates = findCandidates(trip, needsPMR, withPet, excludeDriverIds)

        if (candidates.isNotEmpty()) {
            val driverToNotify = candidates.first()

            trip.driver = driverToNotify
            trip.status = TripStatus.Assigned
            tripRepository.save(trip)

            notificationService.notifyDriverOfNewTrip(driverToNotify.driverId, trip)

            LOGGER.info("Viaje ${trip.tripId} ofrecido al conductor ${driverToNotify.driverId}. Esperando aceptación.")
        } else {
            handleNoDriverFound(trip)
        }
    }

    private fun findCandidates(trip: Trip, needsPMR: Boolean, withPet: Boolean, excludeDriverIds: List<Long>): List<Driver> {
        val candidatesAtStop = geolocationService.findAvailableDriversNearby(
            trip.pickupLocation, 1000.0, DriverRealtimeStatus.AtStop, needsPMR, withPet, excludeDriverIds
        )
        if (candidatesAtStop.isNotEmpty()) return candidatesAtStop

        return geolocationService.findAvailableDriversNearby(
            trip.pickupLocation, 1500.0, DriverRealtimeStatus.Free, needsPMR, withPet, excludeDriverIds
        )
    }

    private fun handleNoDriverFound(trip: Trip) {
        trip.driver = null // Asegurarse de que no quede ningún conductor asignado
        trip.status = TripStatus.Cancelled
        val savedTrip = tripRepository.save(trip)

        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)
        LOGGER.warning("No se encontraron conductores disponibles para el viaje ${trip.tripId}.")
    }
}