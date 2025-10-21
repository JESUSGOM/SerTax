package com.sertax.api.controller

import com.sertax.api.service.AdminDataService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
// TODO: Proteger esta ruta para que solo sea accesible por administradores autenticados
class AdminDataController(private val adminDataService: AdminDataService) {

    @GetMapping("/dashboard")
    fun getDashboardStats(): ResponseEntity<*> {
        val stats = adminDataService.getDashboardStats()
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/drivers")
    fun getAllDrivers(): ResponseEntity<*> {
        val drivers = adminDataService.getAllDrivers()
        return ResponseEntity.ok(drivers)
    }

    @GetMapping("/drivers/{id}")
    fun getDriverDetails(@PathVariable id: Long): ResponseEntity<*> {
        return try {
            val driverDetails = adminDataService.getDriverDetails(id)
            ResponseEntity.ok(driverDetails)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build<String>()
        }
    }

    @GetMapping("/trips")
    fun getTripHistory(): ResponseEntity<*> {
        val trips = adminDataService.getAllTrips()
        return ResponseEntity.ok(trips)
    }

    /**
     * Devuelve una lista de todas las incidencias reportadas en el sistema.
     */
    @GetMapping("/incidents")
    fun getAllIncidents(): ResponseEntity<*> {
        val incidents = adminDataService.getAllIncidents()
        return ResponseEntity.ok(incidents)
    }

    /**
     * Devuelve los detalles completos de una incidencia específica.
     */
    @GetMapping("/incidents/{id}")
    fun getIncidentDetails(@PathVariable id: Long): ResponseEntity<*> {
        return try {
            val incidentDetails = adminDataService.getIncidentDetails(id)
            ResponseEntity.ok(incidentDetails)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build<String>()
        }
    }
}