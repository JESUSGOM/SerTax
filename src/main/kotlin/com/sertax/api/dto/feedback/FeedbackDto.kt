package com.sertax.api.dto.feedback

// DTO para que un usuario cree una nueva valoración
data class CreateRatingRequest(
    val tripId: Long,
    val userId: Long,
    val score: Int, // De 1 a 5
    val comments: String?
)

// DTO para reportar una incidencia
data class CreateIncidentRequest(
    val tripId: Long?,
    val reporterId: Long,
    val reporterType: String, // "User" o "Driver"
    val type: String, // "Queja", "Sugerencia", "Objeto Olvidado"
    val description: String
)