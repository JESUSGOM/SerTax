package com.sertax.api.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "messages")
data class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val messageId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripId", nullable = false)
    val trip: Trip,

    @Column(nullable = false)
    val senderId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val senderType: SenderType,

    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,

    val timestamp: OffsetDateTime = OffsetDateTime.now()
)

enum class SenderType {
    User, Driver
}