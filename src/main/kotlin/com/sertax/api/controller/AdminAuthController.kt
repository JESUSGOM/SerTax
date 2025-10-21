package com.sertax.api.controller

import com.sertax.api.dto.auth.LoginRequest
import com.sertax.api.service.AdminAuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/auth")
class AdminAuthController(private val adminAuthService: AdminAuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<*> {
        return try {
            val token = adminAuthService.login(request)
            ResponseEntity.ok(mapOf("token" to token))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
        }
    }
}