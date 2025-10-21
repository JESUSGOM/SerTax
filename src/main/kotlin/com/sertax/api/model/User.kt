package com.sertax.api.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid")
    val userId: Long = 0,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "email", unique = true, nullable = false, length = 100)
    var email: String,

    @Column(name = "phonenumber", unique = true, nullable = false, length = 20)
    var phoneNumber: String,

    @Column(name = "passwordhash", nullable = false, length = 255)
    var passwordHash: String,

    @Column(name = "legaltermsversion", length = 20)
    var legalTermsVersion: String? = null,

    @Column(name = "isactive")
    var isActive: Boolean = false, // <-- MODIFICADO: El usuario no está activo al registrarse

    @Column(name = "verificationcode") // <-- NUEVO
    var verificationCode: String? = null,

    @Column(name = "verificationcodeexpiresat") // <-- NUEVO
    var verificationCodeExpiresAt: OffsetDateTime? = null,

    @Column(name = "hasseenwizard") // <-- NUEVO
    var hasSeenWizard: Boolean = false,

    @Column(name = "registrationdate", updatable = false)
    val registrationDate: OffsetDateTime = OffsetDateTime.now()
)