package com.sertax.api.controller

import com.sertax.api.dto.driver.DriverStatusUpdateRequest
import com.sertax.api.dto.panic.PanicRequestDto
import com.sertax.api.service.DriverService
import com.sertax.api.service.DriverTripService
import com.sertax.api.service.PanicService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/drivers")
class DriverController(
    private val driverService: DriverService,
    private val driverTripService: DriverTripService,
    private val panicService: PanicService // <-- INYECTADO
) {

    /**
     * Endpoint para que un conductor active el Botón del Pánico.
     */
    @PostMapping("/panic")
    fun triggerPanic(@RequestBody request: PanicRequestDto): ResponseEntity<*> {
        return try {
            panicService.triggerPanicAlert(request)
            ResponseEntity.ok(mapOf("message" to "Alerta de pánico enviada."))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // ... (endpoints de shift, status, accept, etc. sin cambios) ...
    @PostMapping("/{driverId}/shift/start")
    fun startShift(@PathVariable driverId: Long): ResponseEntity<*> { return try { driverService.startShift(driverId).let { ResponseEntity.ok(mapOf("message" to "Turno iniciado.", "newStatus" to it.currentStatus)) } } catch (e: NoSuchElementException) { ResponseEntity.notFound().build<String>() } }
    @PostMapping("/{driverId}/shift/end")
    fun endShift(@PathVariable driverId: Long): ResponseEntity<*> { return try { driverService.endShift(driverId).let { ResponseEntity.ok(mapOf("message" to "Turno finalizado.", "newStatus" to it.currentStatus)) } } catch (e: NoSuchElementException) { ResponseEntity.notFound().build<String>() } }
    @PutMapping("/{driverId}/status")
    fun updateStatus(@PathVariable driverId: Long, @RequestBody request: DriverStatusUpdateRequest): ResponseEntity<*> { return try { driverService.updateDriverStatus(driverId, request).let { ResponseEntity.ok(mapOf("message" to "Estado actualizado", "driverId" to it.driverId, "newStatus" to it.currentStatus)) } } catch (e: Exception) { ResponseEntity.badRequest().body(mapOf("error" to e.message)) } }
    @PostMapping("/{driverId}/trips/{tripId}/accept")
    fun acceptTrip(@PathVariable driverId: Long, @PathVariable tripId: Long): ResponseEntity<*> { return try { driverTripService.acceptTrip(tripId, driverId).let { ResponseEntity.ok(mapOf("message" to "Viaje aceptado.", "newStatus" to it.status)) } } catch (e: Exception) { ResponseEntity.badRequest().body(mapOf("error" to e.message)) } }
    @PostMapping("/{driverId}/trips/{tripId}/start")
    fun startTrip(@PathVariable driverId: Long, @PathVariable tripId: Long): ResponseEntity<*> { return try { driverTripService.startTrip(tripId, driverId).let { ResponseEntity.ok(mapOf("message" to "Viaje iniciado.", "newStatus" to it.status)) } } catch (e: Exception) { ResponseEntity.badRequest().body(mapOf("error" to e.message)) } }
    @PostMapping("/{driverId}/trips/{tripId}/complete")
    fun completeTrip(@PathVariable driverId: Long, @PathVariable tripId: Long, @RequestBody request: Map<String, String>): ResponseEntity<*> { return try { driverTripService.completeTrip(tripId, driverId, BigDecimal(request["finalCost"])).let { ResponseEntity.ok(mapOf("message" to "Viaje completado.", "newStatus" to it.status)) } } catch (e: Exception) { ResponseEntity.badRequest().body(mapOf("error" to e.message)) } }
    @PostMapping("/{driverId}/trips/{tripId}/cancel")
    fun cancelTripByDriver(@PathVariable driverId: Long, @PathVariable tripId: Long): ResponseEntity<*> { return try { driverTripService.cancelTripByDriver(tripId, driverId).let { ResponseEntity.ok(mapOf("message" to "Viaje cancelado.", "tripId" to it.tripId, "newStatus" to it.status)) } } catch (e: Exception) { ResponseEntity.badRequest().body(mapOf("error" to e.message)) } }
    @PostMapping("/{driverId}/trips/{tripId}/reject")
    fun rejectTrip(@PathVariable driverId: Long, @PathVariable tripId: Long): ResponseEntity<*> { return try { driverTripService.rejectTrip(tripId, driverId).let { ResponseEntity.ok(mapOf("message" to "Viaje rechazado.")) } } catch (e: Exception) { ResponseEntity.badRequest().body(mapOf("error" to e.message)) } }

    /**
     * Endpoint para que un conductor reporte que un usuario no se ha presentado.
     */
    @PostMapping("/{driverId}/trips/{tripId}/no-show")
    fun reportNoShow(@PathVariable driverId: Long, @PathVariable tripId: Long): ResponseEntity<*> {
        return try {
            val trip = driverTripService.reportNoShow(tripId, driverId)
            ResponseEntity.ok(mapOf("message" to "Reporte de 'no comparecencia' registrado.", "newStatus" to trip.status))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}