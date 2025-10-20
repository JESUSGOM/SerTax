package com.sertax.api.model

import jakarta.persistence.*

@Entity
@Table(name = "systemadmins")
data class SystemAdmin(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val adminId: Long = 0,

    @Column(unique = true, nullable = false, length = 50)
    val username: String,

    @Column(nullable = false, length = 255)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: AdminRole,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associationId")
    val association: Association?
)

enum class AdminRole {
    GestorMunicipal, AdminMunicipal, Asociacion
}