package com.sertax.api.controller

import com.sertax.api.dto.auth.LoginRequest
import com.sertax.api.dto.auth.RegisterRequest
import com.sertax.api.dto.auth.VerifyRequest
import com.sertax.api.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    /**
     * Endpoint para registrar un nuevo usuario.
     */
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<*> {
        return try {
            val user = authService.register(request)
            ResponseEntity.status(HttpStatus.CREATED).body(mapOf("message" to "Usuario registrado con éxito. Por favor, revisa tu email para verificar tu cuenta.", "userId" to user.userId))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /**
     * Endpoint para verificar la cuenta con el código recibido.
     */
    @PostMapping("/verify") // <-- NUEVO ENDPOINT
    fun verifyAccount(@RequestBody request: VerifyRequest): ResponseEntity<*> {
        return try {
            authService.verify(request)
            ResponseEntity.ok(mapOf("message" to "Cuenta verificada con éxito. Ya puedes iniciar sesión."))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /**
     * Endpoint para el inicio de sesión de un usuario.
     */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<*> {
        return try {
            val token = authService.login(request)
            ResponseEntity.ok(mapOf("token" to token))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
        } catch (e: IllegalStateException) {
            // Captura el error de cuenta no verificada
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        }
    }
}