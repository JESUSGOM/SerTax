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
        private const val LAS_ASSOC_KEY = "LAST_MANUAL_ASSIGNMENT_ASSOC_ID"
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
        trip.driver = null
        trip.status = TripStatus.PendingManualAssignment
        
        // --- LÓGICA DE BALANCEO (ROUND-ROBIN) ---
        val associations = associationRepository.findAll().sortedBy { it.associationId }
        if (associations.isNotEmpty()) {
            val lastAssignedConfig = systemConfigRepository.findByIdOrNull(LAST_ASSOC_KEY)
                ?: SystemConfig(LAST_ASSOC_KEY, "0", "ID de la última asociación asignada.")
            
            val lastAssignedId = lastAssignedConfig.configValue.toLongOrNull() ?: 0L
            val lastIndex = associations.indexOfFirst { it.associationId == lastAssignedId }
            
            val nextIndex = (lastIndex + 1) % associations.size
            val nextAssociation = associations[nextIndex]
            
            // Asignamos el viaje a la siguiente asociación en la rotación
            trip.manualAssignmentAssociation = nextAssociation
            
            // Guardamos el ID de la asociación que acaba de recibir el viaje
            lastAssignedConfig.configValue = nextAssociation.associationId.toString()
            systemConfigRepository.save(lastAssignedConfig)
            
            LOGGER.info("Viaje ${trip.tripId} asignado a la asociación '${nextAssociation.name}' para gestión manual.")
        } else {
            LOGGER.warning("No hay asociaciones configuradas para la asignación manual.")
        }
        
        val savedTrip = tripRepository.save(trip)
        notificationService.notifyUserOfManualAssignment(savedTrip.user.userId, savedTrip)
    }
}