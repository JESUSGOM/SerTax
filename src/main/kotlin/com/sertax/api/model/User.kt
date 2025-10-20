package com.sertax.api.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid") // <-- CORRECCIÓN PRINCIPAL
    val userId: Long = 0,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "email", unique = true, nullable = false, length = 100)
    var email: String,

    @Column(name = "phonenumber", unique = true, nullable = false, length = 20) // <-- CORRECCIÓN
    var phoneNumber: String,

    @Column(name = "passwordhash", nullable = false, length = 255) // <-- CORRECCIÓN
    var passwordHash: String,

    @Column(name = "legaltermsversion", length = 20) // <-- CORRECCIÓN
    var legalTermsVersion: String? = null,

    @Column(name = "isactive") // <-- CORRECCIÓN
    var isActive: Boolean = true,

    @Column(name = "registrationdate", updatable = false) // <-- CORRECCIÓN
    val registrationDate: OffsetDateTime = OffsetDateTime.now()
)