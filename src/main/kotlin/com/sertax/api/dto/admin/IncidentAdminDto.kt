package com.sertax.api.dto.admin

import java.time.OffsetDateTime

// DTO para mostrar una incidencia en la lista del BackOffice
data class IncidentListDto(
    val incidentId: Long,
    val type: String,
    val status: String,
    val reporterType: String,
    val timestamp: OffsetDateTime,
    val tripId: Long?
)

// DTO para ver los detalles completos de una incidencia
data class IncidentDetailDto(
    val incidentId: Long,
    val type: String,
    val status: String,
    val description: String,
    val timestamp: OffsetDateTime,
    val reporter: ReporterInfo,
    val trip: TripInfo?
) {
    data class ReporterInfo(
        val reporterId: Long,
        val reporterType: String,
        val name: String // Nombre del usuario o conductor que reportó
    )

    data class TripInfo(
        val tripId: Long,
        val pickupAddress: String,
        val destinationAddress: String?
    )
}