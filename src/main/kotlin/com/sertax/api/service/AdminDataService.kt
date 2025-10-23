package com.sertax.api.service

import com.sertax.api.dto.admin.*
import com.sertax.api.model.*
import com.sertax.api.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.RoundingMode
// import java.util.logging.Logger // Comentado - No usado

@Service
@Transactional
class AdminDataService(
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
    // companion object { // Comentado - No usado
    //     private val LOGGER = Logger.getLogger(AdminDataService::class.java.name)
    // }
    
    // --- Helper Público ---
    @Transactional(readOnly = true)
    fun getAdmin(username: String): SystemAdmin {
        return systemAdminRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Administrador '$username' no encontrado")
    }
    
    // --- MÉTODOS DE LECTURA (GET) ---
    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getAllLicenses(adminUsername: String): List<License> {
        val admin = getAdmin(adminUsername)
        val assoc: Association? = admin.association // Variable local inmutable para smart cast
        return if (admin.role == AdminRole.Asociacion && assoc != null) {
            licenseRepository.findByAssociation(assoc) // Correcto
        } else {
            licenseRepository.findAll()
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllAssociations(): List<Association> {
        return associationRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getAllVehicles(adminUsername: String): List<Vehicle> {
        val admin = getAdmin(adminUsername)
        val assoc: Association? = admin.association // Variable local inmutable
        return if (admin.role == AdminRole.Asociacion && assoc != null) {
            vehicleRepository.findByLicenseAssociation(assoc) // Corregido: Nombre método camelCase
        } else {
            vehicleRepository.findAll()
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllSystemConfigs(adminUsername: String): List<SystemConfig> {
        val admin = getAdmin(adminUsername)
        if (admin.role != AdminRole.AdminMunicipal) {
            throw AccessDeniedException("No tienes permiso para ver la configuración del sistema.")
        }
        return systemConfigRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getLicenseById(id: Long, adminUsername: String): License {
        val license = licenseRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Licencia con ID $id no encontrada.")
        val admin = getAdmin(adminUsername)
        val assoc: Association? = admin.association // Variable local
        // Corregido: Safe calls ?. y uso de variable local
        if (admin.role == AdminRole.Asociacion && license.association?.associationId != assoc?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver esta licencia.")
        }
        return license
    }
    
    @Transactional(readOnly = true)
    fun getDriverById(id: Long, adminUsername: String): Driver {
        val driver = driverRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Conductor con ID $id no encontrado.")
        val admin = getAdmin(adminUsername)
        val assoc: Association? = admin.association // Variable local
        // Corregido: Safe calls ?. y uso de variable local
        if (admin.role == AdminRole.Asociacion && driver.license.association?.associationId != assoc?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver este conductor.")
        }
        return driver
    }
    
    @Transactional(readOnly = true)
    fun getVehicleById(id: Long, adminUsername: String): Vehicle {
        val vehicle = vehicleRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Vehículo con ID $id no encontrado.")
        val admin = getAdmin(adminUsername)
        val assoc: Association? = admin.association // Variable local
        // Corregido: Safe calls ?. y uso de variable local
        if (admin.role == AdminRole.Asociacion && vehicle.license.association?.associationId != assoc?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver este vehículo.")
        }
        return vehicle
    }
    
    @Transactional(readOnly = true)
    fun getAllDrivers(adminUsername: String): List<DriverListDto> {
        val admin = getAdmin(adminUsername)
        val assoc: Association? = admin.association // Variable local
        val drivers = if (admin.role == AdminRole.Asociacion && assoc != null) {
            driverRepository.findByLicenseAssociation(assoc) // Corregido: Nombre método camelCase
        } else {
            driverRepository.findAll()
        }
        return drivers.map { driver ->
            DriverListDto(
                driverId = driver.driverId,
                name = driver.name,
                role = driver.role.name,
                licenseNumber = driver.license.licenseNumber,
                isActive = driver.isActive
            )
        }
    }
    
    @Transactional(readOnly = true)
    fun getDriverDetails(driverId: Long, adminUsername: String): DriverDetailDto {
        val driver = getDriverById(driverId, adminUsername)
        val vehicle = vehicleRepository.findAll().find { it.license.licenseId == driver.license.licenseId }
        return DriverDetailDto(/* ... */)
    }
    
    @Transactional(readOnly = true)
    fun getDashboardStats(): DashboardStatsDto { /* ... */ }
    
    @Transactional(readOnly = true)
    fun getAllTrips(adminUsername: String): List<TripHistoryDto> {
        val admin = getAdmin(adminUsername)
        val assoc: Association? = admin.association // Variable local
        val allTrips = if (admin.role == AdminRole.Asociacion && assoc != null) {
            tripRepository.findByDriverLicenseAssociation(assoc) // Corregido: Nombre método camelCase
        } else {
            tripRepository.findAll()
        }
        val allRatings = ratingRepository.findAll().associateBy { it.trip.tripId }
        return allTrips.sortedByDescending { it.requestTimestamp }.map { trip ->
            TripHistoryDto(
                tripId = trip.tripId,
                status = trip.status.name,
                requestTimestamp = trip.requestTimestamp,
                pickupAddress = trip.pickupAddress,
                destinationAddress = trip.destinationAddress,
                finalCost = trip.finalCost,
                rating = allRatings[trip.tripId]?.score, // Correcto
                user = TripHistoryDto.UserInfo(
                    userId = trip.user.userId,
                    name = trip.user.name
                ),
                driver = trip.driver?.let { driver ->
                    TripHistoryDto.DriverInfo( // Correcto
                        driverId = driver.driverId,
                        name = driver.name,
                        licenseNumber = driver.license.licenseNumber
                    )
                }
            )
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllIncidents(adminUsername: String): List<IncidentListDto> {
        val admin = getAdmin(adminUsername)
        val assoc: Association? = admin.association // Variable local
        val incidents = if (admin.role == AdminRole.Asociacion && assoc != null) {
            incidentRepository.findByTripDriverLicenseAssociation(assoc) // Corregido: Nombre método camelCase
        } else {
            incidentRepository.findAll()
        }
        return incidents.sortedByDescending { it.timestamp }.map { incident ->
            IncidentListDto(/* ... */)
        }
    }
    
    @Transactional(readOnly = true)
    fun getIncidentDetails(incidentId: Long, adminUsername: String): IncidentDetailDto {
        val incident = incidentRepository.findByIdOrNull(incidentId)
            ?: throw NoSuchElementException("Incidencia con ID $incidentId no encontrada")
        val admin = getAdmin(adminUsername)
        val assoc: Association? = admin.association // Variable local
        // Corregido: Safe calls ?. y uso de variable local
        if (admin.role == AdminRole.Asociacion && incident.trip?.driver?.license?.association?.associationId != assoc?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver esta incidencia.")
        }
        val reporterName = when (incident.reporterType) { /* ... */ }
        return IncidentDetailDto(/* ... */)
    }
    
    @Transactional(readOnly = true)
    fun getRatingStats(): RatingStatsDto { /* ... */ }
    
    @Transactional(readOnly = true)
    fun getPendingManualTrips(adminUsername: String): List<Trip> {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.AdminMunicipal || admin.role == AdminRole.GestorMunicipal) {
            return tripRepository.findByStatus(TripStatus.PendingManualAssignment)
        }
        val assoc: Association? = admin.association // Variable local
        if (admin.role == AdminRole.Asociacion && assoc != null) {
            return tripRepository.findByStatusAndManualAssignmentAssociation(TripStatus.PendingManualAssignment, assoc) // Corregido: Usa variable local
        }
        return emptyList()
    }
    
    @Transactional(readOnly = true)
    fun getAvailableDriversForAssociation(adminUsername: String): List<Driver> {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.AdminMunicipal || admin.role == AdminRole.GestorMunicipal) {
            // Corregido: Nombre método camelCase
            return driverRepository.findByIsActiveTrueAndDriverStatusCurrentStatusIn(
                listOf(DriverRealtimeStatus.Free, DriverRealtimeStatus.AtStop)
            )
        }
        val assoc: Association? = admin.association // Variable local
        if (admin.role == AdminRole.Asociacion && assoc != null) {
            // Corregido: Nombre método camelCase y usa variable local
            return driverRepository.findByIsActiveTrueAndLicenseAssociationAssociationIdAndDriverStatusCurrentStatusIn(
                assoc.associationId,
                listOf(DriverRealtimeStatus.Free, DriverRealtimeStatus.AtStop)
            )
        }
        return emptyList()
    }
    
    // --- MÉTODOS DE GESTIÓN (CRUD) ---
    fun createUser(request: CreateUserRequestDto, adminUsername: String): User { /* ... */ }
    fun updateUser(userId: Long, request: UpdateUserRequestDto, adminUsername: String): User { /* ... */ }
    fun createLicense(request: CreateLicenseRequestDto, adminUsername: String): License { /* ... */ }
    fun updateLicense(id: Long, request: UpdateLicenseRequestDto, adminUsername: String): License {
        val admin = getAdmin(adminUsername)
        if (admin.role != AdminRole.AdminMunicipal) throw AccessDeniedException("Solo AdminMunicipal puede modificar licencias.")
        val license = getLicenseById(id, adminUsername)
        licenseRepository.findByLicenseNumber(request.licenseNumber)?.let { if (it.licenseId != id) throw IllegalStateException("El número de licencia ya existe.") }
        license.licenseNumber = request.licenseNumber // Corregido: Ahora funciona porque licenseNumber es 'var'
        license.association = request.associationId?.let { if (it == -1L) null else associationRepository.findByIdOrNull(it) } // Corregido: Ahora funciona porque association es 'var'
        return licenseRepository.save(license)
    }
    fun createDriver(request: CreateDriverRequestDto, adminUsername: String): Driver { /* ... */ }
    fun updateDriver(id: Long, request: UpdateDriverRequestDto, adminUsername: String): Driver { /* ... */ }
    fun createVehicle(request: CreateVehicleRequestDto, adminUsername: String): Vehicle {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede crear vehículos.")
        val license = getLicenseById(request.licenseId, adminUsername)
        val assoc: Association? = admin.association // Variable local
        if (admin.role == AdminRole.Asociacion && license.association?.associationId != assoc?.associationId) { // Corregido: Safe calls ?. y uso de variable local
            throw AccessDeniedException("No puedes asignar un vehículo a una licencia que no pertenece a tu asociación.")
        }
        if (vehicleRepository.findByLicensePlate(request.licensePlate) != null) throw IllegalStateException("La matrícula ya está registrada.")
        if (vehicleRepository.findAll().any { it.license.licenseId == request.licenseId }) throw IllegalStateException("Esta licencia ya tiene un vehículo asociado.")
        val newVehicle = Vehicle(
            license = license,
            licensePlate = request.licensePlate,
            make = request.make,
            model = request.model,
            isPMRAdapted = request.isPMRAdapted,
            allowsPets = request.allowsPets,
            photoUrl = null // Corregido: Añadido parámetro faltante photoUrl
        )
        return vehicleRepository.save(newVehicle)
    }
    fun updateVehicle(id: Long, request: UpdateVehicleRequestDto, adminUsername: String): Vehicle { /* ... */ }
    fun updateSystemConfig(key: String, request: UpdateSystemConfigRequestDto, adminUsername: String): SystemConfig { /* ... */ }
    fun updateIncidentStatus(id: Long, newStatus: IncidentStatus, adminUsername: String): Incident {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede gestionar incidencias.")
        // Reusa getIncidentDetails para validar el acceso a la incidencia
        getIncidentDetails(id, adminUsername) // Esta llamada ya valida el acceso por asociación si aplica
        
        // Recuperamos la entidad real para actualizarla
        val currentIncident = incidentRepository.findByIdOrNull(id)!! // Sabemos que existe por la llamada anterior
        currentIncident.status = newStatus
        return incidentRepository.save(currentIncident) // Corregido: Se elimina la variable 'incident' no usada
    }
    fun assignDriverManually(tripId: Long, driverId: Long, adminUsername: String): Trip { /* ... */ }
}