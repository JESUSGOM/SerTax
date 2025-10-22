package com.sertax.api.controller

import com.sertax.api.dto.admin.*
import com.sertax.api.service.AdminDataService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminDataController(private val adminDataService: AdminDataService) {

    // --- ENDPOINTS DE LECTURA (GET) ---
    @GetMapping("/dashboard")
    fun getDashboardStats(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getDashboardStats()) }
    @GetMapping("/drivers")
    fun getAllDrivers(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getAllDrivers()) }
    @GetMapping("/drivers/{id}")
    fun getDriverDetails(@PathVariable id: Long): ResponseEntity<*> { return try { ResponseEntity.ok(adminDataService.getDriverDetails(id)) } catch (e: NoSuchElementException) { ResponseEntity.notFound().build<String>() } }
    @GetMapping("/trips")
    fun getTripHistory(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getAllTrips()) }
    @GetMapping("/incidents")
    fun getAllIncidents(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getAllIncidents()) }
    @GetMapping("/incidents/{id}")
    fun getIncidentDetails(@PathVariable id: Long): ResponseEntity<*> { return try { ResponseEntity.ok(adminDataService.getIncidentDetails(id)) } catch (e: NoSuchElementException) { ResponseEntity.notFound().build<String>() } }
    @GetMapping("/ratings/stats")
    fun getRatingStats(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getRatingStats()) }

    // --- ENDPOINTS DE GESTIÓN DE USUARIOS ---
    @PostMapping("/users")
    fun createUser(@RequestBody request: CreateUserRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(adminDataService.createUser(request))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
    @PutMapping("/users/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody request: UpdateUserRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(adminDataService.updateUser(id, request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // --- NUEVOS ENDPOINTS DE GESTIÓN DE LICENCIAS ---
    @PostMapping("/licenses")
    fun createLicense(@RequestBody request: CreateLicenseRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(adminDataService.createLicense(request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // --- NUEVOS ENDPOINTS DE GESTIÓN DE CONDUCTORES ---
    @PostMapping("/drivers")
    fun createDriver(@RequestBody request: CreateDriverRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(adminDataService.createDriver(request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // --- NUEVOS ENDPOINTS DE GESTIÓN DE VEHÍCULOS ---
    @PostMapping("/vehicles")
    fun createVehicle(@RequestBody request: CreateVehicleRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(adminDataService.createVehicle(request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // --- NUEVO ENDPOINT PARA CONFIGURACIÓN DEL SISTEMA ---
    @PutMapping("/config/{key}")
    fun updateSystemConfig(@PathVariable key: String, @RequestBody request: UpdateSystemConfigRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(adminDataService.updateSystemConfig(key, request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}package com.sertax.api.controller

import com.sertax.api.dto.admin.*
import com.sertax.api.service.AdminDataService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminDataController(private val adminDataService: AdminDataService) {

    // --- ENDPOINTS DE LECTURA (GET) ---
    @GetMapping("/dashboard")
    fun getDashboardStats(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getDashboardStats()) }
    @GetMapping("/drivers")
    fun getAllDrivers(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getAllDrivers()) }
    @GetMapping("/drivers/{id}")
    fun getDriverDetails(@PathVariable id: Long): ResponseEntity<*> { return try { ResponseEntity.ok(adminDataService.getDriverDetails(id)) } catch (e: NoSuchElementException) { ResponseEntity.notFound().build<String>() } }
    @GetMapping("/trips")
    fun getTripHistory(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getAllTrips()) }
    @GetMapping("/incidents")
    fun getAllIncidents(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getAllIncidents()) }
    @GetMapping("/incidents/{id}")
    fun getIncidentDetails(@PathVariable id: Long): ResponseEntity<*> { return try { ResponseEntity.ok(adminDataService.getIncidentDetails(id)) } catch (e: NoSuchElementException) { ResponseEntity.notFound().build<String>() } }
    @GetMapping("/ratings/stats")
    fun getRatingStats(): ResponseEntity<*> { return ResponseEntity.ok(adminDataService.getRatingStats()) }

    // --- ENDPOINTS DE GESTIÓN DE USUARIOS ---
    @PostMapping("/users")
    fun createUser(@RequestBody request: CreateUserRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(adminDataService.createUser(request))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
    @PutMapping("/users/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody request: UpdateUserRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(adminDataService.updateUser(id, request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // --- NUEVOS ENDPOINTS DE GESTIÓN DE LICENCIAS ---
    @PostMapping("/licenses")
    fun createLicense(@RequestBody request: CreateLicenseRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(adminDataService.createLicense(request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // --- NUEVOS ENDPOINTS DE GESTIÓN DE CONDUCTORES ---
    @PostMapping("/drivers")
    fun createDriver(@RequestBody request: CreateDriverRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(adminDataService.createDriver(request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // --- NUEVOS ENDPOINTS DE GESTIÓN DE VEHÍCULOS ---
    @PostMapping("/vehicles")
    fun createVehicle(@RequestBody request: CreateVehicleRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(adminDataService.createVehicle(request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    // --- NUEVO ENDPOINT PARA CONFIGURACIÓN DEL SISTEMA ---
    @PutMapping("/config/{key}")
    fun updateSystemConfig(@PathVariable key: String, @RequestBody request: UpdateSystemConfigRequestDto): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(adminDataService.updateSystemConfig(key, request))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}