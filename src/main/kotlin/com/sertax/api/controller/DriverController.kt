package com.sertax.api.controller

import com.sertax.api.dto.driver.DriverStatusUpdateRequest
import com.sertax.api.service.DriverService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/drivers")
class DriverController(private val driverService: DriverService) {

    /**
     * Endpoint para que un conductor actualice su estado (Libre, Ocupado, etc.) y su ubicación.
     */
    @PutMapping("/{driverId}/status")
    fun updateStatus(@PathVariable driverId: Long, @RequestBody request: DriverStatusUpdateRequest): ResponseEntity<*> {
        return try {
            val updatedStatus = driverService.updateDriverStatus(driverId, request)
            ResponseEntity.ok(mapOf("message" to "Estado actualizado", "driverId" to updatedStatus.driverId, "newStatus" to updatedStatus.currentStatus))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build<String>()
        } catch (e: IllegalArgumentException) {
            // Esto podría ocurrir si el string del estado en la petición no es válido ("Free", "Busy", etc.).
            ResponseEntity.badRequest().body(mapOf("error" to "El estado proporcionado no es válido."))
        }
    }

    /*
     * Aquí irían los endpoints para que un conductor gestione un viaje, como:
     *
     * @PostMapping("/{driverId}/trips/{tripId}/accept")
     * fun acceptTrip(@PathVariable driverId: Long, @PathVariable tripId: Long): ResponseEntity<*> { ... }
     *
     * @PostMapping("/{driverId}/trips/{tripId}/complete")
     * fun completeTrip(@PathVariable driverId: Long, @PathVariable tripId: Long): ResponseEntity<*> { ... }
     */
}