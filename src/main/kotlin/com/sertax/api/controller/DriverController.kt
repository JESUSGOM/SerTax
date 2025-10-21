package com.sertax.api.controller

import com.sertax.api.dto.driver.DriverStatusUpdateRequest
import com.sertax.api.service.DriverService
import com.sertax.api.service.DriverTripService // <-- NUEVA IMPORTACIÓN
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/drivers")
class DriverController(
    private val driverService: DriverService,
    private val driverTripService: DriverTripService // <-- INYECTAMOS EL NUEVO SERVICIO
) {

    /**
     * Endpoint para que un conductor actualice su estado (Libre, Ocupado, etc.) y su ubicación.
     */
    @PutMapping("/{driverId}/status")
    fun updateStatus(@PathVariable driverId: Long, @RequestBody request: DriverStatusUpdateRequest): ResponseEntity<*> {
        // ... (sin cambios)
        return try {
            val updatedStatus = driverService.updateDriverStatus(driverId, request)
            ResponseEntity.ok(mapOf("message" to "Estado actualizado", "driverId" to updatedStatus.driverId, "newStatus" to updatedStatus.currentStatus))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build<String>()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "El estado proporcionado no es válido."))
        }
    }

    // --- NUEVOS ENDPOINTS PARA GESTIONAR VIAJES ---

    /**
     * Endpoint para que un conductor acepte un viaje que le ha sido asignado.
     */
    @PostMapping("/{driverId}/trips/{tripId}/accept")
    fun acceptTrip(@PathVariable driverId: Long, @PathVariable tripId: Long): ResponseEntity<*> {
        return try {
            val trip = driverTripService.acceptTrip(tripId, driverId)
            ResponseEntity.ok(mapOf("message" to "Viaje aceptado. Dirígete al punto de recogida.", "newStatus" to trip.status))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /**
     * Endpoint para que el conductor indique que ha recogido al pasajero y el viaje comienza.
     */
    @PostMapping("/{driverId}/trips/{tripId}/start")
    fun startTrip(@PathVariable driverId: Long, @PathVariable tripId: Long): ResponseEntity<*> {
        return try {
            val trip = driverTripService.startTrip(tripId, driverId)
            ResponseEntity.ok(mapOf("message" to "Viaje iniciado.", "newStatus" to trip.status))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /**
     * Endpoint para que el conductor finalice el viaje e introduzca el coste final.
     */
    @PostMapping("/{driverId}/trips/{tripId}/complete")
    fun completeTrip(
        @PathVariable driverId: Long,
        @PathVariable tripId: Long,
        @RequestBody request: Map<String, String> // Espera un JSON como: { "finalCost": "12.50" }
    ): ResponseEntity<*> {
        return try {
            val finalCost = BigDecimal(request["finalCost"])
            val trip = driverTripService.completeTrip(tripId, driverId, finalCost)
            ResponseEntity.ok(mapOf("message" to "Viaje completado.", "newStatus" to trip.status, "finalCost" to trip.finalCost))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}