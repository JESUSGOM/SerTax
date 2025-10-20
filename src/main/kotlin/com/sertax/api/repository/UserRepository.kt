package com.sertax.api.repository

import com.sertax.api.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    // Spring Data JPA crea automáticamente la consulta para buscar por email o teléfono
    fun findByEmail(email: String): User?
    fun findByPhoneNumber(phoneNumber: String): User?
}