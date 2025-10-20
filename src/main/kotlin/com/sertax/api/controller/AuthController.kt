package com.sertax.api.controller


import com.sertax.api.dto.auth.LoginRequest
import com.sertax.api.dto.auth.RegisterRequest
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
     * Valida los datos y devuelve un mensaje de éxito o error.
     */
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<*> {
        return try {
            val user = authService.register(request)
            // Devuelve una respuesta de éxito con el ID del nuevo usuario.
            ResponseEntity.status(HttpStatus.CREATED).body(mapOf("message" to "Usuario registrado con éxito", "userId" to user.userId))
        } catch (e: IllegalStateException) {
            // Devuelve un error si el email o teléfono ya existen.
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /**
     * Endpoint para el inicio de sesión de un usuario.
     * Valida las credenciales.
     */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<*> {
        return try {
            val token = authService.login(request)
            // --- INICIO DE LA CORRECCIÓN ---
            // Antes devolvía un mensaje, ahora devuelve el token real.
            ResponseEntity.ok(mapOf("token" to token))
            // --- FIN DE LA CORRECCIÓN ---
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
        }
    }
}