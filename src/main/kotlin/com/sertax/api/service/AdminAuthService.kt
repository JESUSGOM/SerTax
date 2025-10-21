package com.sertax.api.service

import com.sertax.api.config.JwtProvider
import com.sertax.api.dto.auth.LoginRequest
import com.sertax.api.repository.SystemAdminRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AdminAuthService(
    private val systemAdminRepository: SystemAdminRepository,
    private val passwordEncoder: PasswordEncoder
    // TODO: Considerar inyectar JwtProvider para generar un token real
) {
    fun login(request: LoginRequest): String {
        // En el DTO LoginRequest, el campo 'email' se usa para el 'username' del administrador
        val admin = systemAdminRepository.findByUsername(request.email)
            ?: throw IllegalArgumentException("Administrador o contraseña incorrectos.")

        if (!passwordEncoder.matches(request.password, admin.passwordHash)) {
            throw IllegalArgumentException("Administrador o contraseña incorrectos.")
        }

        // En una implementación futura, generarías un token JWT con roles de administrador.
        // Por ahora, devolvemos un token de marcador de posición para la prueba.
        // return jwtProvider.generateAdminToken(admin)
        return "fake-admin-jwt-token-for-${admin.username}"
    }
}