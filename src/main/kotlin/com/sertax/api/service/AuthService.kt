package com.sertax.api.service

import com.sertax.api.config.JwtProvider
import com.sertax.api.dto.auth.LoginRequest
import com.sertax.api.dto.auth.RegisterRequest
import com.sertax.api.dto.auth.VerifyRequest
import com.sertax.api.model.User
import com.sertax.api.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import kotlin.random.Random

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
    // En un futuro, inyectarías un NotificationService para enviar el email
) {

    /**
     * Registra un nuevo usuario pero lo deja inactivo.
     * Genera un código de verificación y lo guarda.
     */
    fun register(request: RegisterRequest): User {
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalStateException("El email ya está en uso.")
        }
        if (userRepository.findByPhoneNumber(request.phoneNumber) != null) {
            throw IllegalStateException("El número de teléfono ya está en uso.")
        }

        val verificationCode = Random.nextInt(100000, 999999).toString()

        val newUser = User(
            name = request.name,
            email = request.email,
            phoneNumber = request.phoneNumber,
            passwordHash = passwordEncoder.encode(request.password),
            legalTermsVersion = request.legalTermsVersion,
            isActive = false, // La cuenta empieza inactiva
            verificationCode = verificationCode,
            verificationCodeExpiresAt = OffsetDateTime.now().plusMinutes(15) // Código expira en 15 mins
        )

        // Aquí iría la lógica para enviar el email con el código
        println("CÓDIGO DE VERIFICACIÓN PARA ${newUser.email}: $verificationCode") // Simulación

        return userRepository.save(newUser)
    }

    /**
     * Verifica una cuenta usando el email y el código.
     * Si el código es correcto, activa la cuenta.
     */
    fun verify(request: VerifyRequest): User {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Usuario no encontrado.")

        if (user.isActive) {
            throw IllegalStateException("La cuenta ya está verificada.")
        }

        if (user.verificationCodeExpiresAt?.isBefore(OffsetDateTime.now()) == true) {
            throw IllegalStateException("El código de verificación ha expirado.")
        }

        if (user.verificationCode != request.code) {
            throw IllegalArgumentException("Código incorrecto.")
        }

        user.isActive = true
        user.verificationCode = null // Se limpia el código tras su uso
        user.verificationCodeExpiresAt = null

        return userRepository.save(user)
    }

    /**
     * Realiza el login, pero solo si la cuenta del usuario está activa.
     */
    fun login(request: LoginRequest): String {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Usuario o contraseña incorrectos.")

        if (!user.isActive) {
            throw IllegalStateException("La cuenta no ha sido verificada. Por favor, revisa tu email.")
        }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Usuario o contraseña incorrectos.")
        }

        return jwtProvider.generateToken(user)
    }
}