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
    private val systemAdminRepository: SystemAdminRepository,
    private val notificationService: NotificationService
) {

    // --- MÉTODOS DE LECTURA (GET) ---
    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getAllLicenses(): List<License> {
        return licenseRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getAllAssociations(): List<Association> {
        return associationRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getAllVehicles(): List<Vehicle> {
        return vehicleRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getAllSystemConfigs(): List<SystemConfig> {
        return systemConfigRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getLicenseById(id: Long): License {
        return licenseRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Licencia con ID $id no encontrada.")
    }

    @Transactional(readOnly = true)
    fun getDriverById(id: Long): Driver {
        return driverRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Conductor con ID $id no encontrado.")
    }

    @Transactional(readOnly = true)
    fun getVehicleById(id: Long): Vehicle {
        return vehicleRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Vehículo con ID $id no encontrado.")
    }

    @Transactional(readOnly = true)
    fun getAllDrivers(): List<DriverListDto> {
        return driverRepository.findAll().map { driver ->
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
    fun getDriverDetails(driverId: Long): DriverDetailDto {
        val driver = driverRepository.findByIdOrNull(driverId) ?: throw NoSuchElementException("Conductor con ID $driverId no encontrado")
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
    fun getAllTrips(): List<TripHistoryDto> {
        val allTrips = tripRepository.findAll().sortedByDescending { it.requestTimestamp }
        val allRatings = ratingRepository.findAll().associateBy { it.trip.tripId }
        return allTrips.map { trip ->
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
    fun getAllIncidents(): List<IncidentListDto> {
        return incidentRepository.findAll().sortedByDescending { it.timestamp }.map { incident ->
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
    fun getIncidentDetails(incidentId: Long): IncidentDetailDto {
        val incident = incidentRepository.findByIdOrNull(incidentId) ?: throw NoSuchElementException("Incidencia con ID $incidentId no encontrada")
        val reporterName = when (incident.reporterType) {
            ReporterType.User -> userRepository.findByIdOrNull(incident.reporterId)?.name ?: "Usuario no encontrado"
            ReporterType.Driver -> driverRepository.findByIdOrNull(incident.reporterId)?.name ?: "Conductor no encontrado"
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
        val allRatings = ratingRepository.findAll()
        if (allRatings.isEmpty()) {
            return RatingStatsDto(0, java.math.BigDecimal.ZERO, emptyMap())
        }
        val totalRatings = allRatings.size
        val average = allRatings.map { it.score }.average()
        val distribution = allRatings.groupBy { it.score }.mapValues { it.value.size.toLong() }
        return RatingStatsDto(
            totalRatings = totalRatings,
            overallAverageRating = java.math.BigDecimal(average).setScale(2, RoundingMode.HALF_UP),
            ratingDistribution = distribution
        )
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