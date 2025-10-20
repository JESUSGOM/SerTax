package com.sertax.api.repository

import com.sertax.api.model.Trip
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : JpaRepository<Trip, Long> {
    // El nombre correcto que coincide con la ruta Trip -> user -> userId
    fun findByUserUserId(userId: Long): List<Trip>

    // También corrige este
    fun findByDriverDriverId(driverId: Long): List<Trip>
}