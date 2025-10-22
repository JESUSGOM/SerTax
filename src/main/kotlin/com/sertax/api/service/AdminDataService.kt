package com.sertax.api.service

import com.sertax.api.dto.admin.*
import com.sertax.api.model.DriverRealtimeStatus
import com.sertax.api.model.ReporterType
import com.sertax.api.model.TripStatus
import com.sertax.api.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.RoundingMode

@Service
class AdminDataService(
    private val driverRepository: DriverRepository,
    private val vehicleRepository: VehicleRepository,
    private val tripRepository: TripRepository,
    private val driverStatusRepository: DriverStatusRepository,
    private val userRepository: UserRepository,
    private val incidentRepository: IncidentRepository,
    private val ratingRepository: RatingRepository // <-- INYECTADO
) {

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
        val driver = driverRepository.findByIdOrNull(driverId)
            ?: throw NoSuchElementException("Conductor con ID $driverId no encontrado")
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

    /**
     * Devuelve el historial de viajes, incluyendo la puntuación de cada uno si existe.
     */
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
                rating = allRatings[trip.tripId]?.score, // <-- ASIGNAMOS LA PUNTUACIÓN
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
        val incident = incidentRepository.findByIdOrNull(incidentId)
            ?: throw NoSuchElementException("Incidencia con ID $incidentId no encontrada")
        val reporterName = when (incident.reporterType) {
            ReporterType.User -> userRepository.findByIdOrNull(incident.reporterId)?.name ?: "Usuario no encontrado"
            ReporterType.Driver -> driverRepository.findByIdOrNull(incident.reporterId)?.name ?: "Conductor no encontrado"
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

    /**
     * Calcula y devuelve estadísticas agregadas sobre todas las valoraciones del sistema.
     */
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
}