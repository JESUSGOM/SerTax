package com.sertax.api.service

import com.sertax.api.model.Driver
import com.sertax.api.model.DriverRealtimeStatus
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Service

@Service
class GeolocationService(
    @PersistenceContext private val entityManager: EntityManager
) {
    fun findAvailableDriversNearby(
        pickupLocation: Point,
        radiusInMeters: Double,
        status: DriverRealtimeStatus,
        needsPMR: Boolean,
        withPet: Boolean,
        excludeDriverIds: List<Long>, // <-- AÑADIDO: Lista de IDs a excluir
        limit: Int = 5
    ): List<Driver> {
        var queryString = """
            SELECT d.* FROM drivers d
            JOIN driverstatus ds ON d.driver_id = ds.driver_id
            JOIN licenses l ON d.license_id = l.license_id
            JOIN vehicles v ON l.license_id = v.license_id
            WHERE ds.current_status = :status
              AND ST_DWithin(
                    ds.current_location::geography,
                    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                    :radius
                  )
        """
        if (needsPMR) {
            queryString += " AND v.ispmradapted = TRUE"
        }
        if (withPet) {
            queryString += " AND v.allowspets = TRUE"
        }
        // Añadimos la condición para excluir conductores si la lista no está vacía
        if (excludeDriverIds.isNotEmpty()) {
            queryString += " AND d.driver_id NOT IN (:excludeIds)"
        }
        queryString += """
            ORDER BY ST_Distance(ds.current_location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography)
            LIMIT :limit
        """

        val query = entityManager.createNativeQuery(queryString.trimIndent(), Driver::class.java)

        query.setParameter("lon", pickupLocation.x)
        query.setParameter("lat", pickupLocation.y)
        query.setParameter("radius", radiusInMeters)
        query.setParameter("status", status.name)
        query.setParameter("limit", limit)

        if (excludeDriverIds.isNotEmpty()) {
            query.setParameter("excludeIds", excludeDriverIds)
        }

        @Suppress("UNCHECKED_CAST")
        return query.resultList as List<Driver>
    }
}