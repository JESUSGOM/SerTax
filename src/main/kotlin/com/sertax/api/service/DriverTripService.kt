package com.sertax.api.service

import com.sertax.api.model.ReporterType
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
    private val notificationService: NotificationService,
    private val tripService: TripService,
    private val assignmentService: AssignmentService // <-- INYECTADO
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
        if (trip.status != TripStatus.Assigned) throw IllegalStateException("Este viaje no puede ser aceptado.")
        trip.status = TripStatus.EnRoute
        val savedTrip = tripRepository.save(trip)
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)
        return savedTrip
    }

    @Transactional
    fun startTrip(tripId: Long, driverId: Long): Trip {
        val trip = findTripAndValidateDriver(tripId, driverId)
        if (trip.status != TripStatus.EnRoute) throw IllegalStateException("El conductor debe estar en ruta para iniciar el viaje.")
        trip.status = TripStatus.InProgress
        trip.pickupTimestamp = OffsetDateTime.now()
        val savedTrip = tripRepository.save(trip)
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)
        return savedTrip
    }

    @Transactional
    fun completeTrip(tripId: Long, driverId: Long, finalCost: BigDecimal): Trip {
        val trip = findTripAndValidateDriver(tripId, driverId)
        if (trip.status != TripStatus.InProgress) throw IllegalStateException("No se puede completar un viaje que no está en curso.")
        trip.status = TripStatus.Completed
        trip.finalCost = finalCost
        trip.completionTimestamp = OffsetDateTime.now()
        val savedTrip = tripRepository.save(trip)
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)
        return savedTrip
    }

    @Transactional
    fun cancelTripByDriver(tripId: Long, driverId: Long): Trip {
        findTripAndValidateDriver(tripId, driverId)
        return tripService.cancelTrip(tripId, driverId, ReporterType.Driver)
    }

    /**
     * Permite a un conductor rechazar una oferta de viaje.
     * El sistema buscará automáticamente un nuevo conductor.
     */
    @Transactional
    fun rejectTrip(tripId: Long, driverId: Long): Trip {
        val trip = findTripAndValidateDriver(tripId, driverId)

        if (trip.status != TripStatus.Assigned) {
            throw IllegalStateException("Solo se puede rechazar un viaje que está pendiente de aceptación.")
        }

        // Notificar al usuario que estamos buscando otro conductor
        notificationService.notifyUserOfDriverRejection(trip.user.userId, trip)

        // Liberar el viaje del conductor actual
        trip.driver = null
        trip.assignmentTimestamp = null
        trip.status = TripStatus.Requested
        val savedTrip = tripRepository.save(trip)

        // Volver a lanzar el proceso de asignación, excluyendo al conductor que ha rechazado
        val needsPMR = trip.options?.get("needsPMR") as? Boolean ?: false
        val withPet = trip.options?.get("withPet") as? Boolean ?: false
        assignmentService.findAndAssignDriverForTrip(savedTrip, needsPMR, withPet, excludeDriverIds = listOf(driverId))

        return savedTrip
    }

    /**
     * Reporta que un usuario no se ha presentado en el punto de recogida.
     */
    @Transactional
    fun reportNoShow(tripId: Long, driverId: Long): Trip {
        val trip = findTripAndValidateDriver(tripId, driverId)

        if (trip.status != TripStatus.EnRoute) {
            throw IllegalStateException("Solo se puede reportar una no comparecencia si estás en ruta hacia el cliente.")
        }

        trip.status = TripStatus.NoShow
        val savedTrip = tripRepository.save(trip)

        // Registrar una incidencia automática para el seguimiento del Ayuntamiento
        val incident = Incident(
            trip = savedTrip,
            reporterId = driverId,
            reporterType = ReporterType.System,
            type = "No Comparecencia de Usuario",
            description = "El conductor ${trip.driver?.name} reportó que el usuario ${trip.user.name} no se presentó en el punto de recogida."
        )
        incidentRepository.save(incident)

        // Notificar al usuario que su viaje ha sido cancelado por no presentarse
        notificationService.notifyUserOfNoShow(savedTrip.user.userId, savedTrip)

        return savedTrip
    }
}