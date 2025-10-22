package com.sertax.api.controller

import com.sertax.api.service.FleetService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/fleet")
// TODO: Proteger esta ruta para que solo los conductores autenticados puedan acceder
class FleetController(private val fleetService: FleetService) {

    /**
     * Devuelve la ubicación y estado de otros conductores activos para ser mostrados en el mapa.
     * @param driverId El ID del conductor que hace la petición, para excluirlo de la lista.
     */
    @GetMapping("/drivers/{driverId}")
    fun getNearbyDrivers(@PathVariable driverId: Long): ResponseEntity<*> {
        val drivers = fleetService.getNearbyDrivers(driverId)
        return ResponseEntity.ok(drivers)
    }

    /**
     * Devuelve una lista de todas las paradas de taxi y el número de vehículos en cada una.
     */
    @GetMapping("/stops")
    fun getTaxiStopsStatus(): ResponseEntity<*> {
        val stops = fleetService.getTaxiStopStatus()
        return ResponseEntity.ok(stops)
    }
}