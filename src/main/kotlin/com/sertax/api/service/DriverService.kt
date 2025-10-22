package com.sertax.api.service

import com.sertax.api.dto.driver.DriverStatusUpdateRequest
import com.sertax.api.model.DriverRealtimeStatus
import com.sertax.api.model.DriverStatus
import com.sertax.api.repository.DriverRepository
import com.sertax.api.repository.DriverStatusRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class DriverService(
    private val driverStatusRepository: DriverStatusRepository,
    private val driverRepository: DriverRepository,
    private val taxiStopRepository: TaxiStopRepository // <-- INYECTADO
) {
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
    private val STOP_DETECTION_RADIUS = 50.0 // Radio en metros para detectar una parada

    /**
     * Inicia el turno de un conductor, marcándolo como activo y disponible.
     */
    @Transactional
    fun startShift(driverId: Long): DriverStatus {
        val driver = driverRepository.findByIdOrNull(driverId)
            ?: throw NoSuchElementException("Conductor con ID $driverId no encontrado.")

        driver.isActive = true
        driverRepository.save(driver)

        // Actualizamos o creamos su estado a 'Libre'
        val status = driverStatusRepository.findById(driverId).orElse(
            DriverStatus(driverId = driverId, driver = driver, currentStatus = DriverRealtimeStatus.Free, lastUpdate = OffsetDateTime.now())
        )
        status.currentStatus = DriverRealtimeStatus.Free
        status.lastUpdate = OffsetDateTime.now()
        return driverStatusRepository.save(status)
    }

    /**
     * Finaliza el turno de un conductor, marcándolo como inactivo y fuera de servicio.
     */
    @Transactional
    fun endShift(driverId: Long): DriverStatus {
        val driver = driverRepository.findByIdOrNull(driverId)
            ?: throw NoSuchElementException("Conductor con ID $driverId no encontrado.")

        driver.isActive = false
        driverRepository.save(driver)

        // Actualizamos o creamos su estado a 'Fuera de Servicio'
        val status = driverStatusRepository.findById(driverId).orElse(
            DriverStatus(driverId = driverId, driver = driver, currentStatus = DriverRealtimeStatus.OutOfService, lastUpdate = OffsetDateTime.now())
        )
        status.currentStatus = DriverRealtimeStatus.OutOfService
        status.lastUpdate = OffsetDateTime.now()
        return driverStatusRepository.save(status)
    }

    /**
     * Actualiza el estado y la ubicación de un conductor.
     * Incluye lógica para detectar automáticamente si el conductor está en una parada de taxi.
     */
    @Transactional
    fun updateDriverStatus(driverId: Long, request: DriverStatusUpdateRequest): DriverStatus {
        val driver = driverRepository.findById(driverId)
            .orElseThrow { NoSuchElementException("Conductor con ID $driverId no encontrado.") }

        val currentLocation = geometryFactory.createPoint(Coordinate(request.longitude, request.latitude))
        var statusEnum = DriverRealtimeStatus.valueOf(request.status)

        // --- LÓGICA DE DETECCIÓN AUTOMÁTICA DE PARADA ---
        // Si el conductor se pone en 'Libre', comprobamos si está cerca de una parada oficial.
        if (statusEnum == DriverRealtimeStatus.Free) {
            val nearbyStops = taxiStopRepository.findStopsNear(request.latitude, request.longitude, STOP_DETECTION_RADIUS)
            if (nearbyStops.isNotEmpty()) {
                // Si está en una parada, su estado real es 'En Parada'
                statusEnum = DriverRealtimeStatus.AtStop
            }
        }

        val driverStatus = driverStatusRepository.findById(driverId)
            .orElse(DriverStatus(driverId = driverId, driver = driver, currentStatus = statusEnum, lastUpdate = OffsetDateTime.now()))

        driverStatus.currentStatus = statusEnum
        driverStatus.currentLocation = currentLocation
        driverStatus.lastUpdate = OffsetDateTime.now()

        return driverStatusRepository.save(driverStatus)
    }
}