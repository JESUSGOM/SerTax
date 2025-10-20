package com.sertax.api.service

import com.sertax.api.model.Driver
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Service

@Service
class GeolocationService(
    @PersistenceContext private val entityManager: EntityManager
) {

    /**
     * Encuentra los conductores disponibles dentro de un radio específico (en metros).
     * Utiliza una consulta nativa de SQL para aprovechar la potencia de PostGIS.
     */
    fun findAvailableDriversNearby(pickupLocation: Point, radiusInMeters: Double): List<Driver> {
        val query = entityManager.createNativeQuery(
            """
            SELECT d.* FROM drivers d
            JOIN driverstatus ds ON d.driver_id = ds.driver_id
            WHERE ds.current_status = 'Free'
              AND ST_DWithin(
                    ds.current_location::geography,
                    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                    :radius
                  )
            ORDER BY ST_Distance(ds.current_location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography)
            LIMIT 10
            """.trimIndent(),
            Driver::class.java // Mapea el resultado a la entidad Driver
        )

        query.setParameter("lon", pickupLocation.x)
        query.setParameter("lat", pickupLocation.y)
        query.setParameter("radius", radiusInMeters)

        return query.resultList as List<Driver>
    }
}