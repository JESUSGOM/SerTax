package com.sertax.api.dto.trip

import java.math.BigDecimal
import java.time.OffsetDateTime

// DTO para solicitar un nuevo viaje
data class CreateTripRequest(
    val userId: Long,
    val pickupAddress: String,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val destinationAddress: String?,
    val destinationLatitude: Double?,
    val destinationLongitude: Double?,
    val numPassengers: Int = 1
)

// DTO para representar la información de un viaje a devolver por la API
data class TripResponse(
    val tripId: Long,
    val status: String,
    val requestTimestamp: OffsetDateTime,
    val pickupAddress: String,
    val destinationAddress: String?,
    val finalCost: BigDecimal?,
    val driverInfo: DriverInfo? // Sub-DTO con info del conductor
) {
    data class DriverInfo(
        val name: String,
        val vehicleModel: String,
        val licensePlate: String
    )
}