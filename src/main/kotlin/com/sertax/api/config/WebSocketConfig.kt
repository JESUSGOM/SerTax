package com.sertax.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // Define el endpoint principal al que se conectarán las apps.
        // setAllowedOriginPatterns("*") permite conexiones desde cualquier origen (útil para desarrollo).
        // withSockJS() proporciona un fallback para navegadores que no soportan WebSockets nativos.
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // "/topic" es el prefijo para los canales a los que los clientes se suscriben para recibir mensajes del servidor.
        // Ej: /topic/user/123
        registry.enableSimpleBroker("/topic")
        // "/app" es el prefijo para los mensajes que los clientes envían al servidor.
        registry.setApplicationDestinationPrefixes("/app")
    }
}