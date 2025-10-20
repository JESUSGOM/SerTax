package com.sertax.api.service

import com.sertax.api.config.JwtProvider
import com.sertax.api.dto.auth.LoginRequest
import com.sertax.api.dto.auth.RegisterRequest
import com.sertax.api.model.User
import com.sertax.api.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider // Inyecta el proveedor de JWT
) {

    /**
     * Registra un nuevo usuario en el sistema.
     * Valida que el email y el teléfono no existan previamente.
     * Encripta la contraseña antes de guardarla.
     */
    fun register(request: RegisterRequest): User {
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalStateException("El email ya está en uso.")
        }
        if (userRepository.findByPhoneNumber(request.phoneNumber) != null) {
            throw IllegalStateException("El número de teléfono ya está en uso.")
        }

        val newUser = User(
            name = request.name,
            email = request.email,
            phoneNumber = request.phoneNumber,
            // ¡Importante! Encripta la contraseña antes de guardarla
            passwordHash = passwordEncoder.encode(request.password)
        )

        return userRepository.save(newUser)
    }

    /**
     * Valida las credenciales de un usuario. Si son correctas, genera y devuelve un token JWT.
     */
    fun login(request: LoginRequest): String { // Devuelve un String (el token)
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Usuario o contraseña incorrectos.")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Usuario o contraseña incorrectos.")
        }

        // Genera y devuelve el token JWT real
        return jwtProvider.generateToken(user)
    }
}