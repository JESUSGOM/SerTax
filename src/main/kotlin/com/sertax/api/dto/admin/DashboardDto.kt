package com.sertax.api.dto.admin

import java.time.OffsetDateTime

// DTO principal para la respuesta del endpoint del Dashboard del BackOffice
data class DashboardStatsDto(
    val liveStats: LiveStats,
    val recentActivity: RecentActivity
) {
    // Contiene las estadísticas en tiempo real
    data class LiveStats(
        val activeDrivers: Int,
        val driversAtStop: Int,
        val driversOnPickup: Int,
        val tripsInProgress: Int
    )

    // Contiene las listas de actividad más reciente
    data class RecentActivity(
        val lastRegisteredUsers: List<RecentUserDto>,
        val lastCompletedTrips: List<RecentTripDto>
    )
}

// Sub-DTO para mostrar un usuario de la lista de registros recientes
data class RecentUserDto(
    val userId: Long,
    val name: String,
    val registrationDate: OffsetDateTime
)

// Sub-DTO para mostrar un viaje de la lista de viajes recientes
data class RecentTripDto(
    val tripId: Long,
    val pickupAddress: String,
    val driverName: String?,
    val completionTimestamp: OffsetDateTime
)