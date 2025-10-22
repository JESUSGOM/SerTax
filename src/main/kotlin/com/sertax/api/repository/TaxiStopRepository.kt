package com.sertax.api.repository

import com.sertax.api.model.TaxiStop
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TaxiStopRepository : JpaRepository<TaxiStop, Long> {

    /**
     * Busca paradas de taxi que se encuentren dentro de un radio específico (en metros)
     * de un punto geográfico dado. Utiliza una consulta nativa de PostGIS para la eficiencia.
     */
    @Query(
        value = "SELECT * FROM taxistops WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radius)",
        nativeQuery = true
    )
    fun findStopsNear(
        @Param("lat") lat: Double,
        @Param("lon") lon: Double,
        @Param("radius") radius: Double
    ): List<TaxiStop>
}