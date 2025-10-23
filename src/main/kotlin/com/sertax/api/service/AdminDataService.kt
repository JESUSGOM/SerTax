package com.sertax.api.service

import com.sertax.api.dto.admin.*
import com.sertax.api.model.*
import com.sertax.api.repository.*
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AdminDataService(
    // Inyectamos todos los repositorios y servicios necesarios
    private val driverRepository: DriverRepository,
    private val vehicleRepository: VehicleRepository,
    private val tripRepository: TripRepository,
    private val driverStatusRepository: DriverStatusRepository,
    private val userRepository: UserRepository,
    private val incidentRepository: IncidentRepository,
    private val ratingRepository: RatingRepository,
    private val licenseRepository: LicenseRepository,
    private val associationRepository: AssociationRepository,
    private val systemConfigRepository: SystemConfigRepository,
    private val passwordEncoder: PasswordEncoder,
    private val systemAdminRepository: SystemAdminRepository,
    private val notificationService: NotificationService
) {
    
    // --- MÉTODOS DE LECTURA (GET) CON FILTRADO POR ROL ---
    
    @Transactional(readOnly = true)
    fun getAllLicenses(adminUsername: String): List<License> {
        val admin = getAdmin(adminUsername)
        return if (isMunicipalAdmin(admin)) {
            licenseRepository.findAll()
        } else {
            val association = admin.association
            if (admin.role == AdminRole.Asociacion && association != null) {
                // CORREGIDO: Llamada al método correcto
                licenseRepository.findByAssociation(association)
            } else {
                emptyList()
            }
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllVehicles(adminUsername: String): List<Vehicle> {
        val admin = getAdmin(adminUsername)
        return if (isMunicipalAdmin(admin)) {
            vehicleRepository.findAll()
        } else {
            val association = admin.association
            if (admin.role == AdminRole.Asociacion && association != null) {
                // CORREGIDO: Llamada al método correcto
                vehicleRepository.findByLicenseAssociation(association)
            } else {
                emptyList()
            }
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllDrivers(adminUsername: String): List<Driver> {
        val admin = getAdmin(adminUsername)
        return if (isMunicipalAdmin(admin)) {
            driverRepository.findAll()
        } else {
            val association = admin.association
            if (admin.role == AdminRole.Asociacion && association != null) {
                // CORREGIDO: Llamada al método correcto
                driverRepository.findByAssociation(association)
            } else {
                emptyList()
            }
        }
    }
    
    // ... aquí irían el resto de tus métodos (getDriverDetails, getAllTrips, etc.) ...
    // Asegúrate de que usen la misma lógica de 'getAdmin' e 'isMunicipalAdmin' para la seguridad.
    
    
    // --- MÉTODOS DE AYUDA (HELPERS) ---
    
    /**
     * Busca un administrador por su nombre de usuario.
     */
    private fun getAdmin(username: String): SystemAdmin {
        return systemAdminRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Administrador '$username' no encontrado")
    }
    
    /**
     * Comprueba si el rol del administrador es de gestión municipal.
     */
    private fun isMunicipalAdmin(admin: SystemAdmin): Boolean {
        return when (admin.role) {
            AdminRole.AdminMunicipal, AdminRole.GestorMunicipal -> true
            else -> false
        }
    }
    
    /**
     * Asegura que el admin logueado tenga un rol específico o superior.
     */
    private fun ensureAdminRole(username: String, requiredRole: AdminRole) {
        val admin = getAdmin(username)
        // AdminMunicipal siempre tiene permiso
        if (admin.role != AdminRole.AdminMunicipal && admin.role != requiredRole) {
            throw AccessDeniedException("Acción no permitida para el rol ${admin.role}.")
        }
    }
} // <-- Esta es la llave de cierre final de la clase AdminDataService