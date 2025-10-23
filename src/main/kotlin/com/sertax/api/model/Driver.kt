package com.sertax.api.model

import jakarta.persistence.*

@Entity
@Table(name = "drivers")
data class Driver(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val driverId: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licenseId", nullable = false)
    val license: License,
    
    // <-- AÑADIDO: Relación con la entidad Association. Puede ser nulo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id")
    var association: Association? = null,
    
    @Column(nullable = false, length = 100)
    var name: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: DriverRole,
    
    @Column(nullable = false, length = 255)
    var passwordHash: String,
    
    var isActive: Boolean = true
)

enum class DriverRole {
    Owner, Salaried
}