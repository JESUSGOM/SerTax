package com.sertax.api.dto.auth

// DTO para la petición de login
data class LoginRequest(
    val email: String,
    val password: String
)

// DTO para la petición de registro de un nuevo usuario
data class RegisterRequest(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val legalTermsVersion: String // <-- AÑADIDO
)

// DTO para la respuesta de autenticación, devolviendo un token
data class AuthResponse(
    val token: String
)

// DTO para la petición de verificación de cuenta
data class VerifyRequest( // <-- NUEVO
    val email: String,
    val code: String
)