package com.sertax.api.service

import com.sertax.api.dto.trip.CreateTripRequest
import com.sertax.api.model.*
import com.sertax.api.repository.IncidentRepository
import com.sertax.api.repository.TripRepository
import com.sertax.api.repository.UserRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TripService(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val assignmentService: AssignmentService,
    private val notificationService: NotificationService,
    private val incidentRepository: IncidentRepository
) {
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    @Transactional
    fun requestTrip(request: CreateTripRequest): Trip {
        val user = userRepository.findById(request.userId)
            .orElseThrow { NoSuchElementException("Usuario con ID ${request.userId} no encontrado.") }
        val pickupPoint = geometryFactory.createPoint(Coordinate(request.pickupLongitude, request.pickupLatitude))
        val destinationPoint = if (request.destinationLatitude != null && request.destinationLongitude != null) {
            geometryFactory.createPoint(Coordinate(request.destinationLongitude, request.destinationLatitude))
        } else null
        val tripOptions = mapOf("needsPMR" to request.needsPMRVehicle, "withPet" to request.withPet)

        val newTrip = Trip(
            user = user,
            driver = null,
            pickupAddress = request.pickupAddress,
            pickupLocation = pickupPoint,
            destinationAddress = request.destinationAddress,
            destinationLocation = destinationPoint,
            numPassengers = request.numPassengers,
            status = TripStatus.Requested,
            requestChannel = RequestChannel.App,
            options = tripOptions
        )
        val savedTrip = tripRepository.save(newTrip)
        assignmentService.findAndAssignDriverForTrip(savedTrip, request.needsPMRVehicle, request.withPet)
        return savedTrip
    }

    /**
     * Lógica central para cancelar un viaje.
     * @param cancellerType indica si quien cancela es el USUARIO o el CONDUCTOR.
     */
    @Transactional
    fun cancelTrip(tripId: Long, cancellerId: Long, cancellerType: ReporterType): Trip {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Viaje con ID $tripId no encontrado.") }

        if (trip.status == TripStatus.Completed || trip.status == TripStatus.Cancelled) {
            throw IllegalStateException("El viaje ya ha finalizado o ha sido cancelado.")
        }

        when (cancellerType) {
            ReporterType.User -> {
                // El usuario cancela
                if (trip.user.userId != cancellerId) throw SecurityException("No tienes permiso para cancelar este viaje.")

                trip.status = TripStatus.Cancelled
                // Si ya había un conductor asignado, notificarle
                trip.driver?.let { driver ->
                    notificationService.notifyDriverOfCancellation(driver.driverId, trip.tripId)
                }
            }
            ReporterType.Driver -> {
                // El conductor cancela
                if (trip.driver?.driverId != cancellerId) throw SecurityException("No tienes permiso para cancelar este viaje.")

                // Notificar al usuario que el conductor ha cancelado y que se busca uno nuevo.
                notificationService.notifyUserOfDriverCancellation(trip.user.userId, trip)

                // Registrar una incidencia automática por la cancelación del conductor
                val incident = Incident(
                    trip = trip,
                    reporterId = cancellerId,
                    reporterType = ReporterType.System,
                    type = "Cancelación de Conductor",
                    description = "El conductor ${trip.driver?.name} (ID: ${trip.driver?.driverId}) canceló el viaje asignado."
                )
                incidentRepository.save(incident)

                // Reiniciar el viaje para buscar un nuevo conductor
                trip.driver = null
                trip.assignmentTimestamp = null
                trip.status = TripStatus.Requested

                val needsPMR = trip.options?.get("needsPMR") as? Boolean ?: false
                val withPet = trip.options?.get("withPet") as? Boolean ?: false
                assignmentService.findAndAssignDriverForTrip(trip, needsPMR, withPet)
            }
            else -> throw IllegalArgumentException("Tipo de cancelador no válido.")
        }

        return tripRepository.save(trip)
    }
}