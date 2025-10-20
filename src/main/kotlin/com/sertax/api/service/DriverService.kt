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
    private val driverRepository: DriverRepository
) {
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    /**
     * Actualiza el estado y la ubicación de un conductor.
     * Si el conductor no tiene un estado previo, se crea uno nuevo.
     */
    @Transactional
    fun updateDriverStatus(driverId: Long, request: DriverStatusUpdateRequest): DriverStatus {
        val driver = driverRepository.findById(driverId)
            .orElseThrow { NoSuchElementException("Conductor con ID $driverId no encontrado.") }

        val currentLocation = geometryFactory.createPoint(Coordinate(request.longitude, request.latitude))
        val status = DriverRealtimeStatus.valueOf(request.status)

        // Busca el estado actual, si no existe lo crea
        val driverStatus = driverStatusRepository.findById(driverId)
            .orElse(DriverStatus(driverId = driverId, driver = driver, currentStatus = status, lastUpdate = OffsetDateTime.now()))

        // Actualiza los datos
        driverStatus.currentStatus = status
        driverStatus.currentLocation = currentLocation
        driverStatus.lastUpdate = OffsetDateTime.now()

        return driverStatusRepository.save(driverStatus)
    }
}