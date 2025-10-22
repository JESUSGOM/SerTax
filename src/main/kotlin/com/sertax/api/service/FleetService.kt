package com.sertax.api.service

import com.sertax.api.dto.fleet.FleetDriverDto
import com.sertax.api.dto.fleet.TaxiStopStatusDto
import com.sertax.api.model.DriverRealtimeStatus
import com.sertax.api.repository.DriverStatusRepository
import com.sertax.api.repository.TaxiStopRepository
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.sqrt

@Service
class FleetService(
    private val driverStatusRepository: DriverStatusRepository,
    private val taxiStopRepository: TaxiStopRepository
) {
    private val STOP_RADIUS = 50.0 // Radio en metros para considerar que un taxi está "en" una parada

    /**
     * Obtiene la ubicación y estado de todos los conductores activos, excepto el que realiza la petición.
     */
    @Transactional(readOnly = true)
    fun getNearbyDrivers(requesterDriverId: Long): List<FleetDriverDto> {
        return driverStatusRepository.findAll()
            .filter {
                // Filtramos para excluir al propio conductor y a los que están fuera de servicio
                it.driverId != requesterDriverId && it.currentStatus != DriverRealtimeStatus.OutOfService
            }
            .map { status ->
                FleetDriverDto(
                    driverId = status.driverId,
                    latitude = status.currentLocation?.y ?: 0.0,
                    longitude = status.currentLocation?.x ?: 0.0,
                    status = status.currentStatus
                )
            }
    }

    /**
     * Obtiene todas las paradas de taxi y calcula cuántos vehículos hay en cada una.
     */
    @Transactional(readOnly = true)
    fun getTaxiStopStatus(): List<TaxiStopStatusDto> {
        val allStops = taxiStopRepository.findAll()
        val driversAtStops = driverStatusRepository.findByCurrentStatus(DriverRealtimeStatus.AtStop)

        // Mapeamos cada parada a su DTO, calculando el número de taxis
        return allStops.map { stop ->
            val taxiCount = driversAtStops.count { driverStatus ->
                // Verificamos si la ubicación del conductor está dentro del radio de la parada
                isNearby(driverStatus.currentLocation, stop.location, STOP_RADIUS)
            }
            TaxiStopStatusDto(
                stopId = stop.stopId,
                name = stop.name,
                latitude = stop.location.y,
                longitude = stop.location.x,
                taxiCount = taxiCount
            )
        }
    }

    // Función de utilidad para comprobar si un punto está cerca de otro
    private fun isNearby(point1: Point?, point2: Point, radiusInMeters: Double): Boolean {
        if (point1 == null) return false
        // Usamos una aproximación simple. Para mayor precisión se usaría la fórmula de Haversine o PostGIS.
        val distance = point1.distance(point2)
        // La distancia de JTS está en grados, una aproximación es ~111,139 metros por grado.
        return (distance * 111139) <= radiusInMeters
    }
}