package com.sertax.api.model

import jakarta.persistence.*

@Entity
@Table(name = "vehicles")
data class Vehicle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val vehicleId: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licenseId", unique = true, nullable = false)
    val license: License,

    @Column(length = 50)
    var make: String?,

    @Column(length = 50)
    var model: String?,

    @Column(unique = true, nullable = false, length = 15)
    var licensePlate: String,

    @Column(length = 255)
    var photoUrl: String?,

    var isPMRAdapted: Boolean = false
)