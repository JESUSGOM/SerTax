package com.sertax.api.controller

import com.sertax.api.dto.feedback.CreateIncidentRequest
import com.sertax.api.dto.feedback.CreateRatingRequest
import com.sertax.api.service.IncidentService
import com.sertax.api.service.RatingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/feedback")
class FeedbackController(
    private val ratingService: RatingService,
    private val incidentService: IncidentService
) {

    /**
     * Endpoint para que un usuario envíe una valoración sobre un viaje completado.
     */
    @PostMapping("/ratings")
    fun createRating(@RequestBody request: CreateRatingRequest): ResponseEntity<*> {
        return try {
            val rating = ratingService.createRating(request)
            ResponseEntity.status(HttpStatus.CREATED).body(mapOf("message" to "Valoración enviada con éxito", "ratingId" to rating.ratingId))
        } catch (e: Exception) {
            // Captura cualquier excepción de negocio (viaje no encontrado, permiso denegado, etc.).
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /**
     * Endpoint para que un usuario o conductor reporte una incidencia.
     */
    @PostMapping("/incidents")
    fun createIncident(@RequestBody request: CreateIncidentRequest): ResponseEntity<*> {
         return try {
            val incident = incidentService.createIncident(request)
            ResponseEntity.status(HttpStatus.CREATED).body(mapOf("message" to "Incidencia reportada con éxito", "incidentId" to incident.incidentId))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}