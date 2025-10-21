package com.sertax.api.controller

import com.sertax.api.dto.trip.CreateTripRequest
import com.sertax.api.dto.trip.TripEstimateRequest
import com.sertax.api.service.CalculationService
import com.sertax.api.service.TripService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trips")
class TripController(
    private val tripService: TripService,
    private val calculationService: CalculationService
) {

    /**
     * Endpoint para obtener una estimación de coste y tiempo antes de solicitar el viaje.
     */
    @PostMapping("/estimate")
    fun estimateTrip(@RequestBody request: TripEstimateRequest): ResponseEntity<*> {
        return try {
            val estimate = calculationService.estimateTrip(
                request.pickupLatitude, request.pickupLongitude,
                request.destinationLatitude, request.destinationLongitude
            )
            ResponseEntity.ok(estimate)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(mapOf("error" to "No se pudo calcular la ruta en este momento. Inténtalo de nuevo más tarde."))
        }
    }

    /**
     * Endpoint para solicitar un nuevo viaje.
     */
    @PostMapping
    fun requestTrip(@RequestBody request: CreateTripRequest): ResponseEntity<*> {
        return try {
            val trip = tripService.requestTrip(request)
            ResponseEntity.status(HttpStatus.CREATED).body(mapOf("message" to "Buscando conductor...", "tripId" to trip.tripId, "status" to trip.status))
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
}