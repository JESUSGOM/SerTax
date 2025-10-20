package com.sertax.api.repository

import com.sertax.api.model.License
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LicenseRepository : JpaRepository<License, Long> {
    fun findByLicenseNumber(licenseNumber: String): License?
}