package com.sertax.api.dto.driver

// DTO para que un conductor actualice su estado y ubicación
data class DriverStatusUpdateRequest(
    val status: String, // "Free", "Busy", "AtStop", "OutOfService"
    val latitude: Double,
    val longitude: Double
)