package com.sertax.api.repository

import com.sertax.api.model.Trip
import com.sertax.api.model.TripStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : JpaRepository<Trip, Long> {
    fun findByUserUserId(userId: Long): List<Trip>
    fun findByDriverDriverId(driverId: Long): List<Trip>

    /**
     * Devuelve una lista paginada de viajes con un estado específico,
     * ordenados por su fecha de finalización descendente.
     */
    fun findAllByStatusOrderByCompletionTimestampDesc(status: TripStatus, pageable: Pageable): List<Trip>
}