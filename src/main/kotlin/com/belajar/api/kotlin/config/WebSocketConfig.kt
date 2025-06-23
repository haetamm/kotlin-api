package com.belajar.api.kotlin.config

import com.belajar.api.kotlin.websocket.CustomHandshakeHandler
import com.belajar.api.kotlin.websocket.CustomHandshakeInterceptor
import com.belajar.api.kotlin.websocket.WebSocketAuthorizationInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.*

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val customHandshakeInterceptor: CustomHandshakeInterceptor,
    private val webSocketAuthorizationInterceptor: WebSocketAuthorizationInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .addInterceptors(customHandshakeInterceptor)
            .setHandshakeHandler(CustomHandshakeHandler())
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(webSocketAuthorizationInterceptor)
    }
}

