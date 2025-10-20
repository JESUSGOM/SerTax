package com.sertax.api.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "ratings")
data class Rating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val ratingId: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripId", unique = true, nullable = false)
    val trip: Trip,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    val user: User,

    @Column(nullable = false)
    val score: Int,

    @Column(columnDefinition = "TEXT")
    var comments: String?,

    @Column(nullable = false, updatable = false)
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)