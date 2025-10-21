package com.sertax.api.service

import com.sertax.api.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {

    /**
     * Marca el flag del wizard como visto para un usuario específico.
     */
    @Transactional
    fun markWizardAsSeen(userEmail: String) {
        val user = userRepository.findByEmail(userEmail)
            ?: throw NoSuchElementException("Usuario no encontrado.")

        if (!user.hasSeenWizard) {
            user.hasSeenWizard = true
            userRepository.save(user)
        }
    }

    /**
     * Actualiza la versión de los términos legales aceptados por el usuario.
     */
    @Transactional
    fun acceptTerms(userEmail: String, version: String) {
        val user = userRepository.findByEmail(userEmail)
            ?: throw NoSuchElementException("Usuario no encontrado.")
        user.legalTermsVersion = version
        userRepository.save(user)
    }
}