package com.sertax.api.service

import com.sertax.api.dto.admin.*
import com.sertax.api.model.*
import com.sertax.api.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException // Importar para control de acceso
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.RoundingMode
import java.util.logging.Logger // Necesario para el Logger

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
    private val systemAdminRepository: SystemAdminRepository, // Asegúrate de que está inyectado
    private val notificationService: NotificationService // Asegúrate de que está inyectado
) {
    companion object { // Logger añadido si se necesita en otros métodos
        private val LOGGER = Logger.getLogger(AdminDataService::class.java.name)
    }
    
    // --- Helper Público ---
    /** Obtiene la entidad SystemAdmin por username. Se usa internamente y por el controller. */
    @Transactional(readOnly = true)
    fun getAdmin(username: String): SystemAdmin { // Corregido: hecho público
        return systemAdminRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Administrador '$username' no encontrado")
    }
    
    
    // --- MÉTODOS DE LECTURA (GET) ---
    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> {
        // Asumimos que solo AdminMunicipal puede ver todos los usuarios
        // La validación de rol se hará en el controlador que llama a este método
        return userRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getAllLicenses(adminUsername: String): List<License> {
        val admin = getAdmin(adminUsername)
        // Si es de asociación, filtra; si no, devuelve todas.
        return if (admin.role == AdminRole.Asociacion && admin.association != null) {
            licenseRepository.findByAssociation(admin.association)
        } else {
            licenseRepository.findAll() // AdminMunicipal y GestorMunicipal ven todas
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllAssociations(): List<Association> {
        // Todas las asociaciones son públicas para los selects
        return associationRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getAllVehicles(adminUsername: String): List<Vehicle> {
        val admin = getAdmin(adminUsername)
        return if (admin.role == AdminRole.Asociacion && admin.association != null) {
            vehicleRepository.findByLicense_Association(admin.association)
        } else {
            vehicleRepository.findAll() // AdminMunicipal y GestorMunicipal ven todos
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllSystemConfigs(adminUsername: String): List<SystemConfig> {
        val admin = getAdmin(adminUsername)
        if (admin.role != AdminRole.AdminMunicipal) {
            // Solo AdminMunicipal puede ver la configuración. Gestor y Asociación no.
            throw AccessDeniedException("No tienes permiso para ver la configuración del sistema.")
        }
        return systemConfigRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getLicenseById(id: Long, adminUsername: String): License {
        val license = licenseRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Licencia con ID $id no encontrada.")
        val admin = getAdmin(adminUsername)
        // Valida si el admin de asociación intenta ver una licencia que no es suya
        if (admin.role == AdminRole.Asociacion && license.association?.associationId != admin.association?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver esta licencia.")
        }
        return license
    }
    
    @Transactional(readOnly = true)
    fun getDriverById(id: Long, adminUsername: String): Driver {
        val driver = driverRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Conductor con ID $id no encontrado.")
        val admin = getAdmin(adminUsername)
        // Valida si el admin de asociación intenta ver un conductor que no es suyo
        if (admin.role == AdminRole.Asociacion && driver.license.association?.associationId != admin.association?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver este conductor.")
        }
        return driver
    }
    
    @Transactional(readOnly = true)
    fun getVehicleById(id: Long, adminUsername: String): Vehicle {
        val vehicle = vehicleRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Vehículo con ID $id no encontrado.")
        val admin = getAdmin(adminUsername)
        // Valida si el admin de asociación intenta ver un vehículo que no es suyo
        if (admin.role == AdminRole.Asociacion && vehicle.license.association?.associationId != admin.association?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver este vehículo.")
        }
        return vehicle
    }
    
    @Transactional(readOnly = true)
    fun getAllDrivers(adminUsername: String): List<DriverListDto> {
        val admin = getAdmin(adminUsername)
        val drivers = if (admin.role == AdminRole.Asociacion && admin.association != null) {
            driverRepository.findByLicense_Association(admin.association)
        } else {
            driverRepository.findAll() // AdminMunicipal y GestorMunicipal ven todos
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
        val driver = getDriverById(driverId, adminUsername) // Reutiliza el método con validación
        val vehicle = vehicleRepository.findAll().find { it.license.licenseId == driver.license.licenseId }
        return DriverDetailDto(
            driverId = driver.driverId,
            name = driver.name,
            role = driver.role.name,
            isActive = driver.isActive,
            license = DriverDetailDto.LicenseInfoDto(
                licenseId = driver.license.licenseId,
                licenseNumber = driver.license.licenseNumber,
                associationName = driver.license.association?.name
            ),
            vehicle = vehicle?.let {
                DriverDetailDto.VehicleInfoDto(
                    vehicleId = it.vehicleId,
                    make = it.make,
                    model = it.model,
                    licensePlate = it.licensePlate,
                    isPMRAdapted = it.isPMRAdapted,
                    allowsPets = it.allowsPets
                )
            }
        )
    }
    
    @Transactional(readOnly = true)
    fun getDashboardStats(): DashboardStatsDto {
        // El dashboard es global, no se filtra por asociación
        val allDriverStatuses = driverStatusRepository.findAll()
        val allTrips = tripRepository.findAll()
        val liveStats = DashboardStatsDto.LiveStats(
            activeDrivers = allDriverStatuses.count { it.currentStatus != DriverRealtimeStatus.OutOfService },
            driversAtStop = allDriverStatuses.count { it.currentStatus == DriverRealtimeStatus.AtStop },
            driversOnPickup = allTrips.count { it.status == TripStatus.EnRoute },
            tripsInProgress = allTrips.count { it.status == TripStatus.InProgress }
        )
        val recentUsers = userRepository.findAllByOrderByRegistrationDateDesc(PageRequest.of(0, 5)).map { user ->
            RecentUserDto(userId = user.userId, name = user.name, registrationDate = user.registrationDate)
        }
        val recentTrips = tripRepository.findAllByStatusOrderByCompletionTimestampDesc(TripStatus.Completed, PageRequest.of(0, 5)).map { trip ->
            RecentTripDto(
                tripId = trip.tripId,
                pickupAddress = trip.pickupAddress,
                driverName = trip.driver?.name,
                completionTimestamp = trip.completionTimestamp!!
            )
        }
        val recentActivity = DashboardStatsDto.RecentActivity(lastRegisteredUsers = recentUsers, lastCompletedTrips = recentTrips)
        return DashboardStatsDto(liveStats, recentActivity)
    }
    
    @Transactional(readOnly = true)
    fun getAllTrips(adminUsername: String): List<TripHistoryDto> {
        val admin = getAdmin(adminUsername)
        val allTrips = if (admin.role == AdminRole.Asociacion && admin.association != null) {
            // Filtra viajes donde el conductor pertenece a la asociación del admin
            tripRepository.findByDriver_License_Association(admin.association)
        } else {
            // AdminMunicipal y GestorMunicipal ven todos los viajes
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
                rating = allRatings[trip.tripId]?.score,
                user = TripHistoryDto.UserInfo(
                    userId = trip.user.userId,
                    name = trip.user.name
                ),
                driver = trip.driver?.let { driver ->
                    TripHistoryDto.DriverInfo(
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
        val incidents = if (admin.role == AdminRole.Asociacion && admin.association != null) {
            // Filtra incidencias donde el conductor (si existe en el viaje) pertenece a la asociación
            incidentRepository.findAll().filter {
                it.trip?.driver?.license?.association?.associationId == admin.association.associationId
            }
            // Alternativa si el repositorio tiene el método directo:
            // incidentRepository.findByTrip_Driver_License_Association(admin.association)
        } else {
            // AdminMunicipal y GestorMunicipal ven todas
            incidentRepository.findAll()
        }
        return incidents.sortedByDescending { it.timestamp }.map { incident ->
            IncidentListDto(
                incidentId = incident.incidentId,
                type = incident.type,
                status = incident.status.name,
                reporterType = incident.reporterType.name,
                timestamp = incident.timestamp,
                tripId = incident.trip?.tripId
            )
        }
    }
    
    @Transactional(readOnly = true)
    fun getIncidentDetails(incidentId: Long, adminUsername: String): IncidentDetailDto {
        val incident = incidentRepository.findByIdOrNull(incidentId)
            ?: throw NoSuchElementException("Incidencia con ID $incidentId no encontrada")
        val admin = getAdmin(adminUsername)
        // Valida si un admin de asociación intenta ver una incidencia que no es de sus conductores
        if (admin.role == AdminRole.Asociacion && incident.trip?.driver?.license?.association?.associationId != admin.association?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver esta incidencia.")
        }
        val reporterName = when (incident.reporterType) {
            ReporterType.User -> userRepository.findByIdOrNull(incident.reporterId)?.name ?: "Usuario (${incident.reporterId})"
            ReporterType.Driver -> driverRepository.findByIdOrNull(incident.reporterId)?.name ?: "Conductor (${incident.reporterId})"
            ReporterType.System -> "Sistema"
        }
        return IncidentDetailDto(
            incidentId = incident.incidentId,
            type = incident.type,
            status = incident.status.name,
            description = incident.description,
            timestamp = incident.timestamp,
            reporter = IncidentDetailDto.ReporterInfo(
                reporterId = incident.reporterId,
                reporterType = incident.reporterType.name,
                name = reporterName
            ),
            trip = incident.trip?.let { trip ->
                IncidentDetailDto.TripInfo(
                    tripId = trip.tripId,
                    pickupAddress = trip.pickupAddress,
                    destinationAddress = trip.destinationAddress
                )
            }
        )
    }
    
    @Transactional(readOnly = true)
    fun getRatingStats(): RatingStatsDto {
        // Estadísticas globales, no se filtran por rol
        val allRatings = ratingRepository.findAll()
        if (allRatings.isEmpty()) return RatingStatsDto(0, java.math.BigDecimal.ZERO, emptyMap())
        val totalRatings = allRatings.size
        val average = allRatings.map { it.score }.average()
        val distribution = allRatings.groupBy { it.score }.mapValues { it.value.size.toLong() }
        return RatingStatsDto(totalRatings, java.math.BigDecimal(average).setScale(2, RoundingMode.HALF_UP), distribution)
    }
    
    @Transactional(readOnly = true)
    fun getPendingManualTrips(adminUsername: String): List<Trip> {
        val admin = getAdmin(adminUsername)
        // Admin municipal ve todos los pendientes
        if (admin.role == AdminRole.AdminMunicipal || admin.role == AdminRole.GestorMunicipal) {
            return tripRepository.findByStatus(TripStatus.PendingManualAssignment)
        }
        // Asociación solo ve los que se le asignaron a ella
        if (admin.role == AdminRole.Asociacion && admin.association != null) {
            return tripRepository.findByStatusAndManualAssignmentAssociation(TripStatus.PendingManualAssignment, admin.association)
        }
        return emptyList() // Otros roles no ven viajes pendientes
    }
    
    @Transactional(readOnly = true)
    fun getAvailableDriversForAssociation(adminUsername: String): List<Driver> {
        val admin = getAdmin(adminUsername)
        // Admin municipal ve todos los disponibles
        if (admin.role == AdminRole.AdminMunicipal || admin.role == AdminRole.GestorMunicipal) {
            return driverRepository.findByIsActiveTrueAndDriverStatus_CurrentStatusIn(listOf(DriverRealtimeStatus.Free, DriverRealtimeStatus.AtStop))
        }
        // Asociación solo ve sus disponibles
        if (admin.role == AdminRole.Asociacion && admin.association != null) {
            return driverRepository.findByIsActiveTrueAndLicense_Association_AssociationIdAndDriverStatus_CurrentStatusIn(admin.association.associationId, listOf(DriverRealtimeStatus.Free, DriverRealtimeStatus.AtStop))
        }
        return emptyList() // Otros roles no ven conductores disponibles
    }
    
    // --- MÉTODOS DE GESTIÓN (CRUD) ---
    // Añadimos adminUsername a todos los métodos CRUD para validación de permisos
    
    fun createUser(request: CreateUserRequestDto, adminUsername: String): User {
        val admin = getAdmin(adminUsername)
        if (admin.role != AdminRole.AdminMunicipal) throw AccessDeniedException("Solo AdminMunicipal puede crear usuarios.")
        if (userRepository.findByEmail(request.email) != null) throw IllegalStateException("El email ya está en uso.")
        if (userRepository.findByPhoneNumber(request.phoneNumber) != null) throw IllegalStateException("El número de teléfono ya está en uso.")
        val newUser = User(name = request.name, email = request.email, phoneNumber = request.phoneNumber, passwordHash = passwordEncoder.encode(request.password), isActive = true)
        return userRepository.save(newUser)
    }
    
    fun updateUser(userId: Long, request: UpdateUserRequestDto, adminUsername: String): User {
        val admin = getAdmin(adminUsername)
        if (admin.role != AdminRole.AdminMunicipal) throw AccessDeniedException("Solo AdminMunicipal puede modificar usuarios.")
        val user = userRepository.findByIdOrNull(userId) ?: throw NoSuchElementException("Usuario con ID $userId no encontrado.")
        userRepository.findByEmail(request.email)?.let { if (it.userId != userId) throw IllegalStateException("El email ya está en uso por otro usuario.") }
        userRepository.findByPhoneNumber(request.phoneNumber)?.let { if (it.userId != userId) throw IllegalStateException("El teléfono ya está en uso por otro usuario.") }
        user.name = request.name; user.email = request.email; user.phoneNumber = request.phoneNumber; user.isActive = request.isActive
        return userRepository.save(user)
    }
    
    fun createLicense(request: CreateLicenseRequestDto, adminUsername: String): License {
        val admin = getAdmin(adminUsername)
        if (admin.role != AdminRole.AdminMunicipal) throw AccessDeniedException("Solo AdminMunicipal puede crear licencias.")
        if (licenseRepository.findByLicenseNumber(request.licenseNumber) != null) throw IllegalStateException("El número de licencia ya existe.")
        val association = request.associationId?.let { if (it == -1L) null else associationRepository.findByIdOrNull(it) }
        val newLicense = License(licenseNumber = request.licenseNumber, association = association)
        return licenseRepository.save(newLicense)
    }
    
    fun updateLicense(id: Long, request: UpdateLicenseRequestDto, adminUsername: String): License {
        val admin = getAdmin(adminUsername)
        if (admin.role != AdminRole.AdminMunicipal) throw AccessDeniedException("Solo AdminMunicipal puede modificar licencias.")
        val license = getLicenseById(id, adminUsername) // Reusa la validación de lectura
        licenseRepository.findByLicenseNumber(request.licenseNumber)?.let { if (it.licenseId != id) throw IllegalStateException("El número de licencia ya existe.") }
        license.licenseNumber = request.licenseNumber
        license.association = request.associationId?.let { if (it == -1L) null else associationRepository.findByIdOrNull(it) }
        return licenseRepository.save(license)
    }
    
    fun createDriver(request: CreateDriverRequestDto, adminUsername: String): Driver {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede crear conductores.")
        val license = getLicenseById(request.licenseId, adminUsername) // Validar que el admin puede acceder a la licencia
        // Si el admin es de asociación, solo puede crear conductores para SU asociación
        if (admin.role == AdminRole.Asociacion && license.association?.associationId != admin.association?.associationId) {
            throw AccessDeniedException("No puedes asignar una licencia que no pertenece a tu asociación.")
        }
        val newDriver = Driver(name = request.name, passwordHash = passwordEncoder.encode(request.password), role = request.role, license = license, isActive = request.isActive)
        return driverRepository.save(newDriver)
    }
    
    fun updateDriver(id: Long, request: UpdateDriverRequestDto, adminUsername: String): Driver {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede editar conductores.")
        val driver = getDriverById(id, adminUsername) // Reusa la validación de lectura
        // Si el admin es de asociación, solo puede editar conductores de SU asociación (ya validado en getDriverById)
        driver.name = request.name; driver.role = request.role; driver.isActive = request.isActive
        return driverRepository.save(driver)
    }
    
    fun createVehicle(request: CreateVehicleRequestDto, adminUsername: String): Vehicle {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede crear vehículos.")
        val license = getLicenseById(request.licenseId, adminUsername) // Validar acceso a la licencia
        // Si el admin es de asociación, solo puede crear vehículos para SU asociación
        if (admin.role == AdminRole.Asociacion && license.association?.associationId != admin.association?.associationId) {
            throw AccessDeniedException("No puedes asignar un vehículo a una licencia que no pertenece a tu asociación.")
        }
        if (vehicleRepository.findByLicensePlate(request.licensePlate) != null) throw IllegalStateException("La matrícula ya está registrada.")
        if (vehicleRepository.findAll().any { it.license.licenseId == request.licenseId }) throw IllegalStateException("Esta licencia ya tiene un vehículo asociado.")
        val newVehicle = Vehicle(license = license, licensePlate = request.licensePlate, make = request.make, model = request.model, isPMRAdapted = request.isPMRAdapted, allowsPets = request.allowsPets)
        return vehicleRepository.save(newVehicle)
    }
    
    fun updateVehicle(id: Long, request: UpdateVehicleRequestDto, adminUsername: String): Vehicle {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede editar vehículos.")
        val vehicle = getVehicleById(id, adminUsername) // Reusa validación de lectura
        // Si el admin es de asociación, solo puede editar vehículos de SU asociación (ya validado en getVehicleById)
        vehicleRepository.findByLicensePlate(request.licensePlate)?.let { if (it.vehicleId != id) throw IllegalStateException("La matrícula ya está en uso por otro vehículo.") }
        vehicle.licensePlate = request.licensePlate; vehicle.make = request.make; vehicle.model = request.model
        vehicle.isPMRAdapted = request.isPMRAdapted; vehicle.allowsPets = request.allowsPets
        return vehicleRepository.save(vehicle)
    }
    
    fun updateSystemConfig(key: String, request: UpdateSystemConfigRequestDto, adminUsername: String): SystemConfig {
        val admin = getAdmin(adminUsername)
        if (admin.role != AdminRole.AdminMunicipal) throw AccessDeniedException("Solo AdminMunicipal puede modificar la configuración.")
        val config = systemConfigRepository.findByIdOrNull(key) ?: throw NoSuchElementException("Clave '$key' no encontrada.")
        config.configValue = request.configValue
        return systemConfigRepository.save(config)
    }
    
    fun updateIncidentStatus(id: Long, newStatus: IncidentStatus, adminUsername: String): Incident {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede gestionar incidencias.")
        val incident = getIncidentDetails(id, adminUsername) // Reusa validación de lectura
        // Si el admin es de asociación, solo puede gestionar incidencias de SU asociación (ya validado en getIncidentDetails)
        val currentIncident = incidentRepository.findByIdOrNull(id)!! // Sabemos que existe por la línea anterior
        currentIncident.status = newStatus
        return incidentRepository.save(currentIncident)
    }
    
    fun assignDriverManually(tripId: Long, driverId: Long, adminUsername: String): Trip {
        val admin = getAdmin(adminUsername)
        if (admin.role == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede asignar viajes.")
        val trip = tripRepository.findByIdOrNull(tripId) ?: throw NoSuchElementException("Viaje con ID $tripId no encontrado.")
        if (trip.status != TripStatus.PendingManualAssignment) throw IllegalStateException("Este viaje no está pendiente.")
        // Reutilizamos getDriverById que ya valida si el admin puede ver/gestionar ese conductor
        val driver = getDriverById(driverId, adminUsername)
        
        // Validar que el viaje estaba asignado a esta asociación (si aplica)
        if(admin.role == AdminRole.Asociacion && trip.manualAssignmentAssociation?.associationId != admin.association?.associationId){
            throw AccessDeniedException("Este viaje no está asignado a tu asociación para gestión manual.")
        }
        
        trip.driver = driver; trip.status = TripStatus.Assigned; trip.assignmentTimestamp = java.time.OffsetDateTime.now()
        val savedTrip = tripRepository.save(trip)
        notificationService.notifyDriverOfNewTrip(driver.driverId, savedTrip)
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)
        return savedTrip
    }
}