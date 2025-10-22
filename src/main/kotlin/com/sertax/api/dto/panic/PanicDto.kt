package com.sertax.api.dto.panic

// DTO para la petición del botón del pánico
data class PanicRequestDto(
    val driverId: Long,
    val latitude: Double,
    val longitude: Double,
    val message: String? // Mensaje opcional
)