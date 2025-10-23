package com.sertax.api.service

import com.sertax.api.dto.admin.*
import com.sertax.api.model.*
import com.sertax.api.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.RoundingMode

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

    // --- MÉTODOS DE LECTURA (GET) ---
    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getAllLicenses(adminUsername: String): List<License> {
        val admin = getAdmin(adminUsername)
        // Si es Admin/Gestor Municipal, devuelve todas. Si es Asociación, filtra por su asociación.
        return if (isAdminMunicipal(admin)) {
            licenseRepository.findAll()
        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
            licenseRepository.findByAssociation(admin.association)
        } else {
            // Si es Gestor o Asociación sin asociación asignada, no ve licencias (o lanzar error)
            emptyList()
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllAssociations(): List<Association> {
        // Todos los roles pueden ver la lista de asociaciones (para los desplegables)
        return associationRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getAllVehicles(adminUsername: String): List<Vehicle> {
        val admin = getAdmin(adminUsername)
        return if (isAdminMunicipal(admin)) {
            vehicleRepository.findAll()
        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
            vehicleRepository.findByLicense_Association(admin.association)
        } else {
            emptyList()
        }package com.sertax.api.service
        
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
                    
                    // --- MÉTODOS DE LECTURA (GET) CON FILTRADO POR ROL ---
                    
                    @Transactional(readOnly = true)
                    fun getAllUsers(): List<User> {
                        // Asumimos que solo Admin Municipal puede ver la lista completa de usuarios
                        // Aunque no se filtra por asociación, podríamos añadir un chequeo de rol si fuera necesario.
                        return userRepository.findAll()
                    }
                    
                    @Transactional(readOnly = true)
                    fun getAllLicenses(adminUsername: String): List<License> {
                        val admin = getAdmin(adminUsername)
                        // Si es Admin/Gestor Municipal, devuelve todas. Si es Asociación, filtra por su asociación.
                        return if (isAdminMunicipal(admin)) {
                            licenseRepository.findAll()
                        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
                            licenseRepository.findByAssociation(admin.association)
                        } else {
                            // Si es Gestor o Asociación sin asociación asignada, no ve licencias (o lanzar error)
                            emptyList()
                        }
                    }
                    
                    @Transactional(readOnly = true)
                    fun getAllAssociations(): List<Association> {
                        // Todos los roles pueden ver la lista de asociaciones (para los desplegables)
                        return associationRepository.findAll()
                    }
                    
                    @Transactional(readOnly = true)
                    fun getAllVehicles(adminUsername: String): List<Vehicle> {
                        val admin = getAdmin(adminUsername)
                        return if (isAdminMunicipal(admin)) {
                            vehicleRepository.findAll()
                        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
                            vehicleRepository.findByLicense_Association(admin.association)
                        } else {
                            emptyList()
                        }
                    }
                    
                    @Transactional(readOnly = true)
                    fun getAllSystemConfigs(adminUsername: String): List<SystemConfig> {
                        // Solo Admin Municipal puede ver/editar la configuración
                        ensureAdminRole(adminUsername, AdminRole.AdminMunicipal)
                        return systemConfigRepository.findAll()
                    }
                    
                    @Transactional(readOnly = true)
                    fun getLicenseById(id: Long, adminUsername: String): License {
                        val license = licenseRepository.findByIdOrNull(id)
                            ?: throw NoSuchElementException("Licencia con ID $id no encontrada.")
                        val admin = getAdmin(adminUsername)
                        // Solo puede ver si es admin municipal o si la licencia es de su asociación
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
                        // Solo puede ver si es admin municipal o si el conductor es de su asociación
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
                        // Solo puede ver si es admin municipal o si el vehículo es de su asociación
                        if (admin.role == AdminRole.Asociacion && vehicle.license.association?.associationId != admin.association?.associationId) {
                            throw AccessDeniedException("No tienes permiso para ver este vehículo.")
                        }
                        return vehicle
                    }
                    
                    @Transactional(readOnly = true)
                    fun getAllDrivers(adminUsername: String): List<DriverListDto> {
                        val admin = getAdmin(adminUsername)
                        val drivers = if (isAdminMunicipal(admin)) {
                            driverRepository.findAll()
                        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
                            driverRepository.findByLicense_Association(admin.association)
                        } else {
                            emptyList()
                        }
                        // Mapea a DTO después de filtrar
                        return drivers.map { driver ->
                            DriverListDto(
                                driverId = driver.driverId, name = driver.name, role = driver.role.name,
                                licenseNumber = driver.license.licenseNumber, isActive = driver.isActive
                            )
                        }
                    }
                    
                    @Transactional(readOnly = true)
                    fun getDriverDetails(driverId: Long, adminUsername: String): DriverDetailDto {
                        // getDriverById ya incluye la validación de permisos
                        val driver = getDriverById(driverId, adminUsername)
                        val vehicle = vehicleRepository.findAll().find { it.license.licenseId == driver.license.licenseId }
                        return DriverDetailDto(
                            driverId = driver.driverId, name = driver.name, role = driver.role.name, isActive = driver.isActive,
                            license = DriverDetailDto.LicenseInfoDto(
                                licenseId = driver.license.licenseId, licenseNumber = driver.license.licenseNumber,
                                associationName = driver.license.association?.name
                            ),
                            vehicle = vehicle?.let {
                                DriverDetailDto.VehicleInfoDto(
                                    vehicleId = it.vehicleId, make = it.make, model = it.model, licensePlate = it.licensePlate,
                                    isPMRAdapted = it.isPMRAdapted, allowsPets = it.allowsPets
                                )
                            }
                        )
                    }
                    
                    @Transactional(readOnly = true)
                    fun getDashboardStats(): DashboardStatsDto {
                        // El dashboard es global y visible para todos los roles de admin
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
                                tripId = trip.tripId, pickupAddress = trip.pickupAddress, driverName = trip.driver?.name,
                                completionTimestamp = trip.completionTimestamp!!
                            )
                        }
                        val recentActivity = DashboardStatsDto.RecentActivity(lastRegisteredUsers = recentUsers, lastCompletedTrips = recentTrips)
                        return DashboardStatsDto(liveStats, recentActivity)
                    }
                    
                    @Transactional(readOnly = true)
                    fun getAllTrips(adminUsername: String): List<TripHistoryDto> {
                        val admin = getAdmin(adminUsername)
                        val allTrips = if (isAdminMunicipal(admin)) {
                            tripRepository.findAll()
                        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
                            // Filtra viajes donde el conductor pertenece a la asociación del admin
                            tripRepository.findByDriver_License_Association(admin.association)
                        } else {
                            emptyList()
                        }
                        val allRatings = ratingRepository.findAll().associateBy { it.trip.tripId }
                        return allTrips.sortedByDescending { it.requestTimestamp }.map { trip ->
                            TripHistoryDto(
                                tripId = trip.tripId, status = trip.status.name, requestTimestamp = trip.requestTimestamp,
                                pickupAddress = trip.pickupAddress, destinationAddress = trip.destinationAddress,
                                finalCost = trip.finalCost, rating = allRatings[trip.tripId]?.score,
                                user = TripHistoryDto.UserInfo(userId = trip.user.userId, name = trip.user.name),
                                driver = trip.driver?.let { d ->
                                    TripHistoryDto.DriverInfo(driverId = d.driverId, name = d.name, licenseNumber = d.license.licenseNumber)
                                }
                            )
                        }
                    }
                    
                    @Transactional(readOnly = true)
                    fun getAllIncidents(adminUsername: String): List<IncidentListDto> {
                        val admin = getAdmin(adminUsername)
                        val incidents = if (isAdminMunicipal(admin)) {
                            incidentRepository.findAll()
                        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
                            // Filtra incidencias relacionadas con viajes de conductores de su asociación
                            incidentRepository.findByTrip_Driver_License_Association(admin.association)
                        } else {
                            emptyList()
                        }
                        return incidents.sortedByDescending { it.timestamp }.map { incident ->
                            IncidentListDto(
                                incidentId = incident.incidentId, type = incident.type, status = incident.status.name,
                                reporterType = incident.reporterType.name, timestamp = incident.timestamp, tripId = incident.trip?.tripId
                            )
                        }
                    }
                    
                    @Transactional(readOnly = true)
                    fun getIncidentDetails(incidentId: Long, adminUsername: String): IncidentDetailDto {
                        val incident = incidentRepository.findByIdOrNull(incidentId) ?: throw NoSuchElementException("Incidencia con ID $incidentId no encontrada")
                        val admin = getAdmin(adminUsername)
                        // Valida si el admin puede ver esta incidencia (si es municipal o de la asociación del conductor implicado, si existe)
                        if (admin.role == AdminRole.Asociacion && incident.trip?.driver?.license?.association?.associationId != admin.association?.associationId) {
                            throw AccessDeniedException("No tienes permiso para ver los detalles de esta incidencia.")
                        }
                        val reporterName = when (incident.reporterType) {
                            ReporterType.User -> userRepository.findByIdOrNull(incident.reporterId)?.name ?: "Usuario Desconocido"
                            ReporterType.Driver -> driverRepository.findByIdOrNull(incident.reporterId)?.name ?: "Conductor Desconocido"
                            ReporterType.System -> "Sistema"
                        }
                        return IncidentDetailDto(
                            incidentId = incident.incidentId, type = incident.type, status = incident.status.name,
                            description = incident.description, timestamp = incident.timestamp,
                            reporter = IncidentDetailDto.ReporterInfo(reporterId = incident.reporterId, reporterType = incident.reporterType.name, name = reporterName),
                            trip = incident.trip?.let { t ->
                                IncidentDetailDto.TripInfo(tripId = t.tripId, pickupAddress = t.pickupAddress, destinationAddress = t.destinationAddress)
                            }
                        )
                    }
                    
                    @Transactional(readOnly = true)
                    fun getRatingStats(): RatingStatsDto {
                        // Las estadísticas son globales
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
                        return when {
                            isAdminMunicipal(admin) -> tripRepository.findByStatus(TripStatus.PendingManualAssignment)
                            admin.role == AdminRole.Asociacion && admin.association != null ->
                                tripRepository.findByStatusAndManualAssignmentAssociation(TripStatus.PendingManualAssignment, admin.association)
                            else -> emptyList() // Gestor Municipal no ve viajes pendientes
                        }
                    }
                    
                    @Transactional(readOnly = true)
                    fun getAvailableDriversForAssociation(adminUsername: String): List<Driver> {
                        val admin = getAdmin(adminUsername)
                        val availableStatuses = listOf(DriverRealtimeStatus.Free, DriverRealtimeStatus.AtStop)
                        return when {
                            isAdminMunicipal(admin) -> driverRepository.findByIsActiveTrueAndDriverStatus_CurrentStatusIn(availableStatuses)
                            admin.role == AdminRole.Asociacion && admin.association != null ->
                                driverRepository.findByIsActiveTrueAndLicense_Association_AssociationIdAndDriverStatus_CurrentStatusIn(
                                    admin.association.associationId, availableStatuses
                                )
                            else -> emptyList() // Gestor no puede asignar
                        }
                    }
                    
                    // --- MÉTODOS DE GESTIÓN (CRUD) CON VALIDACIÓN DE PERMISOS ---
                    
                    fun createUser(request: CreateUserRequestDto, adminUsername: String): User {
                        ensureAdminRole(adminUsername, AdminRole.AdminMunicipal) // Solo AdminMunicipal
                        if (userRepository.findByEmail(request.email) != null) throw IllegalStateException("Email en uso.")
                        if (userRepository.findByPhoneNumber(request.phoneNumber) != null) throw IllegalStateException("Teléfono en uso.")
                        val newUser = User(name = request.name, email = request.email, phoneNumber = request.phoneNumber, passwordHash = passwordEncoder.encode(request.password), isActive = true)
                        return userRepository.save(newUser)
                    }
                    
                    fun updateUser(userId: Long, request: UpdateUserRequestDto, adminUsername: String): User {
                        ensureAdminRole(adminUsername, AdminRole.AdminMunicipal) // Solo AdminMunicipal
                        val user = userRepository.findByIdOrNull(userId) ?: throw NoSuchElementException("Usuario con ID $userId no encontrado.")
                        // ... (validaciones de email/teléfono) ...
                        user.name = request.name; user.email = request.email; user.phoneNumber = request.phoneNumber; user.isActive = request.isActive
                        return userRepository.save(user)
                    }
                    
                    fun createLicense(request: CreateLicenseRequestDto, adminUsername: String): License {
                        ensureAdminRole(adminUsername, AdminRole.AdminMunicipal) // Solo AdminMunicipal
                        if (licenseRepository.findByLicenseNumber(request.licenseNumber) != null) throw IllegalStateException("Licencia ya existe.")
                        val association = request.associationId?.let { if (it == -1L) null else associationRepository.findByIdOrNull(it) }
                        val newLicense = License(licenseNumber = request.licenseNumber, association = association)
                        return licenseRepository.save(newLicense)
                    }
                    
                    fun updateLicense(id: Long, request: UpdateLicenseRequestDto, adminUsername: String): License {
                        ensureAdminRole(adminUsername, AdminRole.AdminMunicipal) // Solo AdminMunicipal
                        val license = licenseRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Licencia con ID $id no encontrada.")
                        // ... (validación de número de licencia) ...
                        license.licenseNumber = request.licenseNumber
                        license.association = request.associationId?.let { if (it == -1L) null else associationRepository.findByIdOrNull(it) }
                        return licenseRepository.save(license)
                    }
                    
                    fun createDriver(request: CreateDriverRequestDto, adminUsername: String): Driver {
                        val admin = getAdmin(adminUsername)
                        // AdminMunicipal o Asociación pueden crear, pero asociación solo para sus licencias
                        val license = licenseRepository.findByIdOrNull(request.licenseId) ?: throw NoSuchElementException("Licencia no encontrada.")
                        if (admin.role == AdminRole.Asociacion && license.association?.associationId != admin.association?.associationId) {
                            throw AccessDeniedException("No puedes crear un conductor para una licencia que no es de tu asociación.")
                        }
                        val newDriver = Driver(name = request.name, passwordHash = passwordEncoder.encode(request.password), role = request.role, license = license, isActive = request.isActive)
                        return driverRepository.save(newDriver)
                    }
                    
                    fun updateDriver(id: Long, request: UpdateDriverRequestDto, adminUsername: String): Driver {
                        val driver = driverRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Conductor no encontrado.")
                        val admin = getAdmin(adminUsername)
                        // AdminMunicipal o Asociación (solo los suyos) pueden editar
                        if (admin.role == AdminRole.Asociacion && driver.license.association?.associationId != admin.association?.associationId) {
                            throw AccessDeniedException("No puedes editar un conductor que no es de tu asociación.")
                        }
                        driver.name = request.name; driver.role = request.role; driver.isActive = request.isActive
                        return driverRepository.save(driver)
                    }
                    
                    fun createVehicle(request: CreateVehicleRequestDto, adminUsername: String): Vehicle {
                        val admin = getAdmin(adminUsername)
                        val license = licenseRepository.findByIdOrNull(request.licenseId) ?: throw NoSuchElementException("Licencia no encontrada.")
                        // AdminMunicipal o Asociación (solo para sus licencias) pueden crear
                        if (admin.role == AdminRole.Asociacion && license.association?.associationId != admin.association?.associationId) {
                            throw AccessDeniedException("No puedes registrar un vehículo para una licencia que no es de tu asociación.")
                        }
                        if (vehicleRepository.findByLicensePlate(request.licensePlate) != null) throw IllegalStateException("Matrícula ya registrada.")
                        if (vehicleRepository.findAll().any { it.license.licenseId == request.licenseId }) throw IllegalStateException("Licencia ya tiene vehículo.")
                        val newVehicle = Vehicle(license = license, licensePlate = request.licensePlate, make = request.make, model = request.model, isPMRAdapted = request.isPMRAdapted, allowsPets = request.allowsPets)
                        return vehicleRepository.save(newVehicle)
                    }
                    
                    fun updateVehicle(id: Long, request: UpdateVehicleRequestDto, adminUsername: String): Vehicle {
                        val vehicle = vehicleRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Vehículo no encontrado.")
                        val admin = getAdmin(adminUsername)
                        // AdminMunicipal o Asociación (solo los suyos) pueden editar
                        if (admin.role == AdminRole.Asociacion && vehicle.license.association?.associationId != admin.association?.associationId) {
                            throw AccessDeniedException("No puedes editar un vehículo que no es de tu asociación.")
                        }
                        vehicleRepository.findByLicensePlate(request.licensePlate)?.let { if (it.vehicleId != id) throw IllegalStateException("Matrícula en uso.") }
                        vehicle.licensePlate = request.licensePlate; vehicle.make = request.make; vehicle.model = request.model;
                        vehicle.isPMRAdapted = request.isPMRAdapted; vehicle.allowsPets = request.allowsPets
                        return vehicleRepository.save(vehicle)
                    }
                    
                    fun updateSystemConfig(key: String, request: UpdateSystemConfigRequestDto, adminUsername: String): SystemConfig {
                        ensureAdminRole(adminUsername, AdminRole.AdminMunicipal) // Solo AdminMunicipal
                        val config = systemConfigRepository.findByIdOrNull(key) ?: throw NoSuchElementException("Clave '$key' no encontrada.")
                        config.configValue = request.configValue
                        return systemConfigRepository.save(config)
                    }
                    
                    fun updateIncidentStatus(id: Long, newStatus: IncidentStatus, adminUsername: String): Incident {
                        val incident = incidentRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Incidencia con ID $id no encontrada.")
                        val admin = getAdmin(adminUsername)
                        // AdminMunicipal o Asociación (solo las de sus conductores) pueden gestionar
                        if (admin.role == AdminRole.Asociacion && incident.trip?.driver?.license?.association?.associationId != admin.association?.associationId) {
                            throw AccessDeniedException("No tienes permiso para gestionar esta incidencia.")
                        }
                        incident.status = newStatus
                        return incidentRepository.save(incident)
                    }
                    
                    fun assignDriverManually(tripId: Long, driverId: Long, adminUsername: String): Trip {
                        val admin = getAdmin(adminUsername)
                        // Solo AdminMunicipal o Asociación pueden asignar manualmente
                        if (admin.role == AdminRole.GestorMunicipal) {
                            throw AccessDeniedException("Los gestores no pueden asignar viajes manualmente.")
                        }
                        val trip = tripRepository.findByIdOrNull(tripId) ?: throw NoSuchElementException("Viaje con ID $tripId no encontrado.")
                        if (trip.status != TripStatus.PendingManualAssignment) throw IllegalStateException("Viaje no pendiente.")
                        val driver = driverRepository.findByIdOrNull(driverId) ?: throw NoSuchElementException("Conductor con ID $driverId no encontrado.")
                        
                        // Validación de Permisos:
                        if (admin.role == AdminRole.Asociacion && trip.manualAssignmentAssociation?.associationId != admin.association?.associationId) {
                            throw AccessDeniedException("Viaje no asignado a tu asociación.")
                        }
                        if (admin.role == AdminRole.Asociacion && driver.license.association?.associationId != admin.association?.associationId) {
                            throw AccessDeniedException("Conductor no pertenece a tu asociación.")
                        }
                        if (!driver.isActive || driver.driverStatus?.currentStatus !in listOf(DriverRealtimeStatus.Free, DriverRealtimeStatus.AtStop)) {
                            throw IllegalStateException("Conductor no disponible.")
                        }
                        
                        trip.driver = driver
                        trip.status = TripStatus.Assigned
                        trip.assignmentTimestamp = java.time.OffsetDateTime.now()
                        trip.manualAssignmentAssociation = null
                        val savedTrip = tripRepository.save(trip)
                        
                        notificationService.notifyDriverOfNewTrip(driver.driverId, savedTrip)
                        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)
                        
                        return savedTrip
                    }
                    
                    // --- Helper Methods ---
                    internal fun getAdmin(username: String): SystemAdmin {
                        return systemAdminRepository.findByUsername(username)
                            ?: throw UsernameNotFoundException("Administrador '$username' no encontrado")
                    }
                    
                    private fun isAdminMunicipal(admin: SystemAdmin): Boolean {
                        return admin.role == AdminRole.AdminMunicipal || admin.role == AdminRole.GestorMunicipal
                    }
                    
                    /**
                     * Asegura que el admin logueado tenga al menos el rol requerido.
                     * Los AdminMunicipal siempre tienen permiso.
                     * Lanza AccessDeniedException si no cumple.
                     */
                    private fun ensureAdminRole(username: String, requiredRole: AdminRole) {
                        val admin = getAdmin(username)
                        // AdminMunicipal siempre tiene permiso
                        if (admin.role != AdminRole.AdminMunicipal && admin.role != requiredRole) {
                            // Compara la "jerarquía" simple: AdminMunicipal > GestorMunicipal > Asociacion
                            if (requiredRole == AdminRole.GestorMunicipal && admin.role == AdminRole.Asociacion) {
                                throw AccessDeniedException("Acción no permitida para el rol ${admin.role}. Se requiere $requiredRole o superior.")
                            }
                            if (requiredRole == AdminRole.AdminMunicipal && (admin.role == AdminRole.Asociacion || admin.role == AdminRole.GestorMunicipal) ) {
                                throw AccessDeniedException("Acción no permitida para el rol ${admin.role}. Se requiere $requiredRole.")
                            }
                        }
                    }
                }
        
        
    }
    
    @Transactional(readOnly = true)
    fun getAllSystemConfigs(adminUsername: String): List<SystemConfig> {
        // Solo Admin Municipal puede ver/editar la configuración
        ensureAdminRole(adminUsername, AdminRole.AdminMunicipal)
        return systemConfigRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    fun getLicenseById(id: Long, adminUsername: String): License {
        val license = licenseRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("Licencia con ID $id no encontrada.")
        val admin = getAdmin(adminUsername)
        // Solo puede ver si es admin municipal o si la licencia es de su asociación
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
        // Solo puede ver si es admin municipal o si el conductor es de su asociación
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
        // Solo puede ver si es admin municipal o si el vehículo es de su asociación
        if (admin.role == AdminRole.Asociacion && vehicle.license.association?.associationId != admin.association?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver este vehículo.")
        }
        return vehicle
    }
    
    @Transactional(readOnly = true)
    fun getAllDrivers(adminUsername: String): List<DriverListDto> {
        val admin = getAdmin(adminUsername)
        val drivers = if (isAdminMunicipal(admin)) {
            driverRepository.findAll()
        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
            driverRepository.findByLicense_Association(admin.association)
        } else {
            emptyList()
        }
        // Mapea a DTO después de filtrar
        return drivers.map { driver ->
            DriverListDto(
                driverId = driver.driverId, name = driver.name, role = driver.role.name,
                licenseNumber = driver.license.licenseNumber, isActive = driver.isActive
            )
        }
    }
    
    @Transactional(readOnly = true)
    fun getDriverDetails(driverId: Long, adminUsername: String): DriverDetailDto {
        // getDriverById ya incluye la validación de permisos
        val driver = getDriverById(driverId, adminUsername)
        val vehicle = vehicleRepository.findAll().find { it.license.licenseId == driver.license.licenseId }
        return DriverDetailDto(
            driverId = driver.driverId, name = driver.name, role = driver.role.name, isActive = driver.isActive,
            license = DriverDetailDto.LicenseInfoDto(
                licenseId = driver.license.licenseId, licenseNumber = driver.license.licenseNumber,
                associationName = driver.license.association?.name
            ),
            vehicle = vehicle?.let {
                DriverDetailDto.VehicleInfoDto(
                    vehicleId = it.vehicleId, make = it.make, model = it.model, licensePlate = it.licensePlate,
                    isPMRAdapted = it.isPMRAdapted, allowsPets = it.allowsPets
                )
            }
        )
    }
    
    @Transactional(readOnly = true)
    fun getDashboardStats(): DashboardStatsDto {
        // El dashboard es global y visible para todos los roles de admin
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
                tripId = trip.tripId, pickupAddress = trip.pickupAddress, driverName = trip.driver?.name,
                completionTimestamp = trip.completionTimestamp!!
            )
        }
        val recentActivity = DashboardStatsDto.RecentActivity(lastRegisteredUsers = recentUsers, lastCompletedTrips = recentTrips)
        return DashboardStatsDto(liveStats, recentActivity)
    }
    
    @Transactional(readOnly = true)
    fun getAllTrips(adminUsername: String): List<TripHistoryDto> {
        val admin = getAdmin(adminUsername)
        val allTrips = if (isAdminMunicipal(admin)) {
            tripRepository.findAll()
        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
            // Filtra viajes donde el conductor pertenece a la asociación del admin
            tripRepository.findByDriver_License_Association(admin.association)
        } else {
            emptyList()
        }
        val allRatings = ratingRepository.findAll().associateBy { it.trip.tripId }
        return allTrips.sortedByDescending { it.requestTimestamp }.map { trip ->
            TripHistoryDto(
                tripId = trip.tripId, status = trip.status.name, requestTimestamp = trip.requestTimestamp,
                pickupAddress = trip.pickupAddress, destinationAddress = trip.destinationAddress,
                finalCost = trip.finalCost, rating = allRatings[trip.tripId]?.score,
                user = TripHistoryDto.UserInfo(userId = trip.user.userId, name = trip.user.name),
                driver = trip.driver?.let { d ->
                    TripHistoryDto.DriverInfo(driverId = d.driverId, name = d.name, licenseNumber = d.license.licenseNumber)
                }
            )
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllIncidents(adminUsername: String): List<IncidentListDto> {
        val admin = getAdmin(adminUsername)
        val incidents = if (isAdminMunicipal(admin)) {
            incidentRepository.findAll()
        } else if (admin.role == AdminRole.Asociacion && admin.association != null) {
            // Filtra incidencias relacionadas con viajes de conductores de su asociación
            incidentRepository.findByTrip_Driver_License_Association(admin.association)
        } else {
            emptyList()
        }
        return incidents.sortedByDescending { it.timestamp }.map { incident ->
            IncidentListDto(
                incidentId = incident.incidentId, type = incident.type, status = incident.status.name,
                reporterType = incident.reporterType.name, timestamp = incident.timestamp, tripId = incident.trip?.tripId
            )
        }
    }
    
    @Transactional(readOnly = true)
    fun getIncidentDetails(incidentId: Long, adminUsername: String): IncidentDetailDto {
        val incident = incidentRepository.findByIdOrNull(incidentId) ?: throw NoSuchElementException("Incidencia con ID $incidentId no encontrada")
        val admin = getAdmin(adminUsername)
        // Valida si el admin puede ver esta incidencia (si es municipal o de la asociación del conductor implicado, si existe)
        if (admin.role == AdminRole.Asociacion && incident.trip?.driver?.license?.association?.associationId != admin.association?.associationId) {
            throw AccessDeniedException("No tienes permiso para ver los detalles de esta incidencia.")
        }
        val reporterName = when (incident.reporterType) {
            ReporterType.User -> userRepository.findByIdOrNull(incident.reporterId)?.name ?: "Usuario Desconocido"
            ReporterType.Driver -> driverRepository.findByIdOrNull(incident.reporterId)?.name ?: "Conductor Desconocido"
            ReporterType.System -> "Sistema"
        }
        return IncidentDetailDto(
            incidentId = incident.incidentId, type = incident.type, status = incident.status.name,
            description = incident.description, timestamp = incident.timestamp,
            reporter = IncidentDetailDto.ReporterInfo(reporterId = incident.reporterId, reporterType = incident.reporterType.name, name = reporterName),
            trip = incident.trip?.let { t ->
                IncidentDetailDto.TripInfo(tripId = t.tripId, pickupAddress = t.pickupAddress, destinationAddress = t.destinationAddress)
            }
        )
    }
    
    @Transactional(readOnly = true)
    fun getRatingStats(): RatingStatsDto {
        // Las estadísticas son globales
        val allRatings = ratingRepository.findAll()
        if (allRatings.isEmpty()) return RatingStatsDto(0, java.math.BigDecimal.ZERO, emptyMap())
        val totalRatings = allRatings.size
        val average = allRatings.map { it.score }.average()
        val distribution = allRatings.groupBy { it.score }.mapValues { it.value.size.toLong() }
        return RatingStatsDto(totalRatings, java.math.BigDecimal(average).setScale(2, RoundingMode.HALF_UP), distribution)
    }

    // --- MÉTODOS DE GESTIÓN (CRUD) ---
    fun createUser(request: CreateUserRequestDto): User {
        if (userRepository.findByEmail(request.email) != null) throw IllegalStateException("El email ya está en uso.")
        if (userRepository.findByPhoneNumber(request.phoneNumber) != null) throw IllegalStateException("El número de teléfono ya está en uso.")
        val newUser = User(name = request.name, email = request.email, phoneNumber = request.phoneNumber, passwordHash = passwordEncoder.encode(request.password), isActive = true)
        return userRepository.save(newUser)
    }
    fun updateUser(userId: Long, request: UpdateUserRequestDto): User {
        val user = userRepository.findByIdOrNull(userId) ?: throw NoSuchElementException("Usuario con ID $userId no encontrado.")
        userRepository.findByEmail(request.email)?.let { if (it.userId != userId) throw IllegalStateException("El email ya está en uso por otro usuario.") }
        userRepository.findByPhoneNumber(request.phoneNumber)?.let { if (it.userId != userId) throw IllegalStateException("El teléfono ya está en uso por otro usuario.") }
        user.name = request.name
        user.email = request.email
        user.phoneNumber = request.phoneNumber
        user.isActive = request.isActive
        return userRepository.save(user)
    }
    fun createLicense(request: CreateLicenseRequestDto): License {
        if (licenseRepository.findByLicenseNumber(request.licenseNumber) != null) throw IllegalStateException("El número de licencia ya existe.")
        val association = request.associationId?.let { if (it == -1L) null else associationRepository.findByIdOrNull(it) }
        val newLicense = License(licenseNumber = request.licenseNumber, association = association)
        return licenseRepository.save(newLicense)
    }
    fun updateLicense(id: Long, request: UpdateLicenseRequestDto): License {
        val license = licenseRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Licencia con ID $id no encontrada.")
        licenseRepository.findByLicenseNumber(request.licenseNumber)?.let { if (it.licenseId != id) throw IllegalStateException("El número de licencia ya existe.") }
        license.licenseNumber = request.licenseNumber
        license.association = request.associationId?.let { if (it == -1L) null else associationRepository.findByIdOrNull(it) }
        return licenseRepository.save(license)
    }
    fun createDriver(request: CreateDriverRequestDto): Driver {
        val license = licenseRepository.findByIdOrNull(request.licenseId) ?: throw NoSuchElementException("Licencia con ID ${request.licenseId} no encontrada.")
        val newDriver = Driver(name = request.name, passwordHash = passwordEncoder.encode(request.password), role = request.role, license = license, isActive = request.isActive)
        return driverRepository.save(newDriver)
    }
    fun updateDriver(id: Long, request: UpdateDriverRequestDto): Driver {
        val driver = driverRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Conductor con ID $id no encontrado.")
        driver.name = request.name
        driver.role = request.role
        driver.isActive = request.isActive
        return driverRepository.save(driver)
    }
    fun createVehicle(request: CreateVehicleRequestDto): Vehicle {
        val license = licenseRepository.findByIdOrNull(request.licenseId) ?: throw NoSuchElementException("Licencia con ID ${request.licenseId} no encontrada.")
        if (vehicleRepository.findByLicensePlate(request.licensePlate) != null) throw IllegalStateException("La matrícula ya está registrada.")
        if (vehicleRepository.findAll().any { it.license.licenseId == request.licenseId }) throw IllegalStateException("Esta licencia ya tiene un vehículo asociado.")
        val newVehicle = Vehicle(license = license, licensePlate = request.licensePlate, make = request.make, model = request.model, isPMRAdapted = request.isPMRAdapted, allowsPets = request.allowsPets)
        return vehicleRepository.save(newVehicle)
    }
    fun updateVehicle(id: Long, request: UpdateVehicleRequestDto): Vehicle {
        val vehicle = vehicleRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Vehículo con ID $id no encontrado.")
        vehicleRepository.findByLicensePlate(request.licensePlate)?.let { if (it.vehicleId != id) throw IllegalStateException("La matrícula ya está en uso por otro vehículo.") }
        vehicle.licensePlate = request.licensePlate
        vehicle.make = request.make
        vehicle.model = request.model
        vehicle.isPMRAdapted = request.isPMRAdapted
        vehicle.allowsPets = request.allowsPets
        return vehicleRepository.save(vehicle)
    }

    fun updateSystemConfig(key: String, request: UpdateSystemConfigRequestDto): SystemConfig {
        val config = systemConfigRepository.findByIdOrNull(key) ?: throw NoSuchElementException("Clave de configuración '$key' no encontrada.")
        config.configValue = request.configValue
        return systemConfigRepository.save(config)
    }

    fun updateIncidentStatus(id: Long, newStatus: IncidentStatus): Incident {
        val incident = incidentRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Incidencia con ID $id no encontrada.")
        incident.status = newStatus
        return incidentRepository.save(incident)
    }

    // --- MÉTODOS DE LECTURA (GET) ---
    @Transactional(readOnly = true)
    fun getPendingManualTrips(): List<Trip> {
        return tripRepository.findByStatus(TripStatus.PendingManualAssignment)
    }

    @Transactional(readOnly = true)
    fun getAvailableDriversForAssociation(adminUsername: String): List<Driver> {
        val admin = systemAdminRepository.findByUsername(adminUsername)
            ?: throw UsernameNotFoundException("Administrador no encontrado")

        // Los perfiles municipales pueden ver todos los conductores disponibles de todas las asociaciones
        if (admin.role == AdminRole.AdminMunicipal || admin.role == AdminRole.GestorMunicipal) {
            return driverRepository.findByIsActiveTrueAndDriverStatus_CurrentStatusIn(
                listOf(DriverRealtimeStatus.Free, DriverRealtimeStatus.AtStop)
            )
        }

        // El perfil de Asociación solo ve sus propios conductores disponibles
        if (admin.role == AdminRole.Asociacion && admin.association != null) {
            return driverRepository.findByIsActiveTrueAndLicense_Association_AssociationIdAndDriverStatus_CurrentStatusIn(
                admin.association.associationId,
                listOf(DriverRealtimeStatus.Free, DriverRealtimeStatus.AtStop)
            )
        }

        return emptyList()
    }

    // --- MÉTODOS DE GESTIÓN (CRUD) ---
    fun assignDriverManually(tripId: Long, driverId: Long, adminUsername: String): Trip {
        val trip = tripRepository.findByIdOrNull(tripId)
            ?: throw NoSuchElementException("Viaje con ID $tripId no encontrado.")

        if (trip.status != TripStatus.PendingManualAssignment) {
            throw IllegalStateException("Este viaje no está pendiente de asignación manual.")
        }

        val driver = driverRepository.findByIdOrNull(driverId)
            ?: throw NoSuchElementException("Conductor con ID $driverId no encontrado.")

        // TODO: En un futuro, añadir validación de permisos para asegurar que el admin
        // puede asignar a este conductor (ej. misma asociación).

        trip.driver = driver
        trip.status = TripStatus.Assigned // El viaje vuelve al flujo normal, esperando aceptación del conductor
        trip.assignmentTimestamp = java.time.OffsetDateTime.now()
        val savedTrip = tripRepository.save(trip)

        // Notificar al conductor de la nueva asignación
        notificationService.notifyDriverOfNewTrip(driver.driverId, savedTrip)

        // Notificar al usuario que ya se le ha asignado un taxi
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)

        return savedTrip
    }
    
    @Transactional(readOnly = true)
    fun getPendingManualTrips(adminUsername: String): List<Trip> {
        val admin = systemAdminRepository.findByUsername(adminUsername)
            ?: throw UsernameNotFoundException("Administrador no encontrado")
        
        if (admin.role == AdminRole.AdminMunicipal || admin.role == AdminRole.GestorMunicipal) {
            return tripRepository.findByStatus(TripStatus.PendingManualAssignment)
        }
        
        if (admin.role == AdminRole.Asociacion && admin.association != null) {
            return tripRepository.findByStatusAndManualAssignmentAssociation(
                TripStatus.PendingManualAssignment,
                admin.association
            )
        }
        return emptyList()
    }
    
    // --- MÉTODOS DE GESTIÓN (CRUD) ---
    fun assignDriverManually(tripId: Long, driverId: Long, adminUsername: String): Trip {
        val trip = tripRepository.findByIdOrNull(tripId)
            ?: throw NoSuchElementException("Viaje con ID $tripId no encontrado.")
        
        if (trip.status != TripStatus.PendingManualAssignment) {
            throw IllegalStateException("Este viaje no está pendiente de asignación manual.")
        }
        
        val driver = driverRepository.findByIdOrNull(driverId)
            ?: throw NoSuchElementException("Conductor con ID $driverId no encontrado.")
        
        trip.driver = driver
        trip.status = TripStatus.Assigned
        trip.assignmentTimestamp = java.time.OffsetDateTime.now()
        val savedTrip = tripRepository.save(trip)
        
        notificationService.notifyDriverOfNewTrip(driver.driverId, savedTrip)
        notificationService.notifyUserOfTripUpdate(savedTrip.user.userId, savedTrip)
        
        return savedTrip
    }
}