package com.sertax.api.service

import com.sertax.api.dto.trip.CreateTripRequest
import com.sertax.api.model.RequestChannel
import com.sertax.api.model.Trip
import com.sertax.api.model.TripStatus
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
    private val assignmentService: AssignmentService
) {
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

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

        val tripOptions = mapOf(
            "needsPMR" to request.needsPMRVehicle,
            "withPet" to request.withPet
        )

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
            options = tripOptions,
            assignmentTimestamp = null,
            completionTimestamp = null,
            estimatedCost = null,
            finalCost = null,
            pickupTimestamp = null
        )

        val savedTrip = tripRepository.save(newTrip)

        assignmentService.findAndAssignDriverForTrip(savedTrip, request.needsPMRVehicle, request.withPet)

        return savedTrip
    }

    @Transactional
    fun cancelTrip(tripId: Long): Trip {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { NoSuchElementException("Viaje con ID $tripId no encontrado.") }

        if (trip.status == TripStatus.Completed || trip.status == TripStatus.Cancelled) {
            throw IllegalStateException("El viaje ya ha finalizado o ha sido cancelado.")
        }

        trip.status = TripStatus.Cancelled
        return tripRepository.save(trip)
    }
}