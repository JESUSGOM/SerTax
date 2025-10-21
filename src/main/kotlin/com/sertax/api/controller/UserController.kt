package com.sertax.api.controller

import com.sertax.api.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    /**
     * Endpoint para que el usuario autenticado marque el wizard de ayuda como visto.
     */
    @PostMapping("/me/wizard-seen")
    fun markWizardAsSeen(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<*> {
        return try {
            userService.markWizardAsSeen(userDetails.username) // El 'username' en UserDetails es el email
            ResponseEntity.ok(mapOf("message" to "Estado del wizard actualizado."))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build<String>()
        }
    }

    /**
     * Endpoint para que el usuario autenticado acepte una nueva versión de los términos legales.
     */
    @PostMapping("/me/accept-terms")
    fun acceptTerms(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: Map<String, String> // Espera un JSON como: { "version": "1.1" }
    ): ResponseEntity<*> {
        return try {
            val version = request["version"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "El campo 'version' es requerido."))
            userService.acceptTerms(userDetails.username, version)
            ResponseEntity.ok(mapOf("message" to "Términos y condiciones actualizados."))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build<String>()
        }
    }
}