package com.sertax.api.model

import jakarta.persistence.*

@Entity
@Table(name = "licenses")
data class License(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val licenseId: Long = 0,

    @Column(unique = true, nullable = false, length = 50)
    val licenseNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associationId")
    val association: Association?
)