package com.sertax.api.dto.chat

import com.sertax.api.model.SenderType
import java.time.OffsetDateTime

// DTO para un mensaje de chat enviado por un cliente (usuario o conductor) al servidor
data class InboundChatMessageDto(
    val tripId: Long,
    val content: String
)

// DTO para un mensaje de chat que el servidor reenvía a los clientes
data class OutboundChatMessageDto(
    val messageId: Long,
    val tripId: Long,
    val senderId: Long,
    val senderType: SenderType,
    val content: String,
    val timestamp: OffsetDateTime
)