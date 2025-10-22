package com.sertax.api.dto.fleet

import com.sertax.api.model.DriverRealtimeStatus

// DTO para representar la información de otro conductor en el mapa
data class FleetDriverDto(
    val driverId: Long,
    val latitude: Double,
    val longitude: Double,
    val status: DriverRealtimeStatus
)

// DTO para representar el estado de una parada de taxi, incluyendo el número de taxis esperando
data class TaxiStopStatusDto(
    val stopId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val taxiCount: Int // Número de taxis actualmente en la parada
)