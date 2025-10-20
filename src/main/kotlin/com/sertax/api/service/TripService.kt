package com.sertax.api.service

import com.sertax.api.dto.trip.CreateTripRequest
import com.sertax.api.model.RequestChannel
import com.sertax.api.model.Trip
import com.sertax.api.model.TripStatus
import com.sertax.api.repository.DriverRepository
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
    private val driverRepository: DriverRepository
    // En el futuro, inyectarías GeolocationService para la asignación
    // private val geolocationService: GeolocationService
) {

    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    /**
     * Crea una nueva solicitud de viaje.
     */
    @Transactional
    fun requestTrip(request: CreateTripRequest): Trip {
        val user = userRepository.findById(request.userId)
            .orElseThrow { NoSuchElementException("Usuario con ID ${request.userId} no encontrado.") }

        val pickupPoint = geometryFactory.createPoint(Coordinate(request.pickupLongitude, request.pickupLatitude))

        val destinationPoint = if (request.destinationLatitude != null && request.destinationLongitude != null) {
            geometryFactory.createPoint(Coordinate(request.destinationLongitude, request.destinationLatitude))
        } else {
            null
        }

        val newTrip = Trip(
            user = user,
            driver = null, // Se asigna null porque aún no hay conductor
            pickupAddress = request.pickupAddress,
            pickupLocation = pickupPoint,
            destinationAddress = request.destinationAddress,
            destinationLocation = destinationPoint,
            numPassengers = request.numPassengers,
            status = TripStatus.Requested, // Estado inicial
            requestChannel = RequestChannel.App, // Asumimos que viene de la App
            // El resto de campos son opcionales o tienen valores por defecto
            assignmentTimestamp = null,
            completionTimestamp = null,
            estimatedCost = null,
            finalCost = null,
            options = null,
            pickupTimestamp = null
        )

        val savedTrip = tripRepository.save(newTrip)

        // Aquí iría la lógica futura para encontrar y asignar un conductor
        // assignDriverToTrip(savedTrip)

        return savedTrip
    }

    /**
     * Lógica (simplificada) para asignar un conductor a un viaje.
     */
    private fun assignDriverToTrip(trip: Trip) {
        // 1. Llamar a GeolocationService para encontrar el conductor libre más cercano.
        // val nearbyDriver = geolocationService.findNearestAvailableDriver(trip.pickupLocation)

        // 2. Si se encuentra un conductor:
        // trip.driver = nearbyDriver
        // trip.status = TripStatus.Assigned
        // tripRepository.save(trip)

        // 3. Notificar al conductor y al usuario (vía WebSockets o notificaciones push).
    }

    /**
     * Cancela un viaje que no ha finalizado.
     */
    @Transactional
    fun cancelTrip(tripId: Long): Trip {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Viaje con ID $tripId no encontrado.") }

        if (trip.status == TripStatus.Completed || trip.status == TripStatus.Cancelled) {
            throw IllegalStateException("El viaje ya ha finalizado o ha sido cancelado.")
        }

        trip.status = TripStatus.Cancelled
        // Aquí se notificaría al conductor si ya estaba asignado.

        return tripRepository.save(trip)
    }
}