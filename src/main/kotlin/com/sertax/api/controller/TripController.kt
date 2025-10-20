package com.sertax.api.controller

import com.sertax.api.dto.trip.CreateTripRequest
import com.sertax.api.service.TripService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trips")
class TripController(private val tripService: TripService) {

    /**
     * Endpoint para que un usuario solicite un nuevo viaje.
     */
    @PostMapping
    fun requestTrip(@RequestBody request: CreateTripRequest): ResponseEntity<*> {
        return try {
            val trip = tripService.requestTrip(request)
            // Una implementación real devolvería un DTO TripResponse más detallado.
            ResponseEntity.status(HttpStatus.CREATED).body(mapOf("message" to "Viaje solicitado con éxito", "tripId" to trip.tripId, "status" to trip.status))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        }
    }

    /**
     * Endpoint para cancelar un viaje que está en curso.
     */
    @PostMapping("/{tripId}/cancel")
    fun cancelTrip(@PathVariable tripId: Long): ResponseEntity<*> {
        return try {
            val trip = tripService.cancelTrip(tripId)
            ResponseEntity.ok(mapOf("message" to "Viaje cancelado", "tripId" to trip.tripId, "newStatus" to trip.status))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /*
     * Aquí se podrían añadir más endpoints, como:
     *
     * @GetMapping("/{tripId}")
     * fun getTripDetails(@PathVariable tripId: Long): ResponseEntity<*> { ... }
     *
     * @GetMapping("/user/{userId}")
     * fun getUserTripHistory(@PathVariable userId: Long): ResponseEntity<*> { ... }
     */
}