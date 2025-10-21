package com.sertax.api.repository

import com.sertax.api.model.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByPhoneNumber(phoneNumber: String): User?

    /**
     * Devuelve una lista paginada de usuarios ordenados por su fecha de registro descendente.
     */
    fun findAllByOrderByRegistrationDateDesc(pageable: Pageable): List<User>
}