package com.sertax.api.service

import com.sertax.api.dto.feedback.CreateRatingRequest
import com.sertax.api.model.Rating
import com.sertax.api.model.TripStatus
import com.sertax.api.repository.RatingRepository
import com.sertax.api.repository.TripRepository
import com.sertax.api.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RatingService(
    private val ratingRepository: RatingRepository,
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository
) {
    /**
     * Permite a un usuario valorar un viaje.
     * Valida que el viaje esté completado y que el usuario sea quien lo solicitó.
     */
    @Transactional
    fun createRating(request: CreateRatingRequest): Rating {
        val trip = tripRepository.findById(request.tripId)
            .orElseThrow { NoSuchElementException("Viaje con ID ${request.tripId} no encontrado.") }

        val user = userRepository.findById(request.userId)
            .orElseThrow { NoSuchElementException("Usuario con ID ${request.userId} no encontrado.") }

        if (trip.user.userId != user.userId) {
            throw SecurityException("No tienes permiso para valorar este viaje.")
        }

        if (trip.status != TripStatus.Completed) {
            throw IllegalStateException("Solo se pueden valorar viajes completados.")
        }
        
        if (request.score < 1 || request.score > 5) {
            throw IllegalArgumentException("La puntuación debe estar entre 1 y 5.")
        }

        val newRating = Rating(
            trip = trip,
            user = user,
            score = request.score,
            comments = request.comments
        )

        return ratingRepository.save(newRating)
    }
}