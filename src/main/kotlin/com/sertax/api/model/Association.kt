package com.sertax.api.model

import jakarta.persistence.*

@Entity
@Table(name = "associations")
data class Association(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val associationId: Long = 0,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(columnDefinition = "TEXT")
    var contactInfo: String?
)