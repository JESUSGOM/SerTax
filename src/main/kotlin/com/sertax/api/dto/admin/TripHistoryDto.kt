package com.sertax.api.dto.admin

import java.math.BigDecimal
import java.time.OffsetDateTime

// DTO para representar un viaje en el listado del historial del BackOffice
data class TripHistoryDto(
    val tripId: Long,
    val status: String,
    val requestTimestamp: OffsetDateTime,
    val pickupAddress: String,
    val destinationAddress: String?,
    val finalCost: BigDecimal?,
    val rating: Int?, // <-- AÑADIDO: Puntuación del viaje (1-5)
    val user: UserInfo,
    val driver: DriverInfo?
) {
    data class UserInfo(
        val userId: Long,
        val name: String
    )

    data class DriverInfo(
        val driverId: Long,
        val name: String,
        val licenseNumber: String
    )
}