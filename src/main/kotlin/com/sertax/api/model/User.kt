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
    
    // <-- AÑADIDO: Campo para almacenar el rol del usuario (GESTOR_MUNICIPAL, ASOCIACION, etc.)
    @Column(name = "role", nullable = false)
    var role: String,
    
    // <-- AÑADIDO: Relación con la entidad Association. Puede ser nulo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id")
    var association: Association? = null,
    
    @Column(name = "legaltermsversion", length = 20)
    var legalTermsVersion: String? = null,
    
    @Column(name = "isactive")
    var isActive: Boolean = false,
    
    @Column(name = "verificationcode")
    var verificationCode: String? = null,
    
    @Column(name = "verificationcodeexpiresat")
    var verificationCodeExpiresAt: OffsetDateTime? = null,
    
    @Column(name = "hasseenwizard")
    var hasSeenWizard: Boolean = false,
    
    @Column(name = "registrationdate", updatable = false)
    val registrationDate: OffsetDateTime = OffsetDateTime.now()
)