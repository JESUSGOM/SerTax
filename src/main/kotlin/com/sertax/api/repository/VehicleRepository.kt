package com.sertax.api.repository

import com.sertax.api.model.AdminRole
import com.sertax.api.model.SystemConfig
import com.sertax.api.model.Vehicle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import com.sertax.api.model.Association

@Repository
interface VehicleRepository : JpaRepository<Vehicle, Long> {
    fun findByLicensePlate(licensePlate: String): Vehicle?
    
    // <-- AÑADIDO/CORREGIDO: Define el método con el nombre correcto
    fun findByLicenseAssociation(association: Association): List<Vehicle>
}