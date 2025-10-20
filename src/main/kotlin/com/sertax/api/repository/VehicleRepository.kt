package com.sertax.api.repository

import com.sertax.api.model.Vehicle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VehicleRepository : JpaRepository<Vehicle, Long> {
    fun findByLicensePlate(licensePlate: String): Vehicle?
}