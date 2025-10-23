package com.sertax.api.repository

import com.sertax.api.model.License
import com.sertax.api.model.Association
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LicenseRepository : JpaRepository<License, Long> {
    fun findByLicenseNumber(licenseNumber: String): License?
    
    
    /**
     * Busca todas las licencias que pertenecen a una asociación específica.
     */
    fun findByAssociation(association: Association): List<License>
}