package com.sertax.api.repository

import com.sertax.api.model.SystemAdmin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SystemAdminRepository : JpaRepository<SystemAdmin, Long> {
    fun findByUsername(username: String): SystemAdmin?
}