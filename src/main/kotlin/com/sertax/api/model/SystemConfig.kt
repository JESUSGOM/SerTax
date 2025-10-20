package com.sertax.api.model

import jakarta.persistence.*

@Entity
@Table(name = "systemconfig")
data class SystemConfig(
    @Id
    @Column(length = 100)
    val configKey: String,

    @Column(nullable = false, length = 255)
    var configValue: String,

    @Column(columnDefinition = "TEXT")
    var description: String?
)