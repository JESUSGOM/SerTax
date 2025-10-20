package com.sertax.api.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "incidents")
data class Incident(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val incidentId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripId")
    val trip: Trip?,

    @Column(nullable = false)
    val reporterId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val reporterType: ReporterType,

    @Column(length = 50)
    var type: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val description: String,

    @Enumerated(EnumType.STRING)
    var status: IncidentStatus = IncidentStatus.Open,

    val timestamp: OffsetDateTime = OffsetDateTime.now()
)

enum class ReporterType {
    User, Driver
}

enum class IncidentStatus {
    Open, InProgress, Resolved
}