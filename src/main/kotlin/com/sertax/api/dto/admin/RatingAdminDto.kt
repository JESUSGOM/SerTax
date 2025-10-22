package com.sertax.api.dto.admin

import java.math.BigDecimal

// DTO para mostrar estadísticas agregadas sobre las valoraciones
data class RatingStatsDto(
    val totalRatings: Int,
    val overallAverageRating: BigDecimal,
    val ratingDistribution: Map<Int, Long> // Mapa de puntuación a cantidad (ej. 5 estrellas -> 150 votos)
)