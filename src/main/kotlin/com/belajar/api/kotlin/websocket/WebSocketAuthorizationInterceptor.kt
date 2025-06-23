package com.belajar.api.kotlin.websocket

import org.springframework.messaging.*
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Component
class WebSocketAuthorizationInterceptor : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        if (StompCommand.SUBSCRIBE == accessor?.command) {
            val principal = accessor.user as? CustomPrincipal
            val user = principal?.user

            if (user != null) {
                val hasRole = user.authorities.any {
                    it.authority == "ROLE_ADMIN" || it.authority == "ROLE_SUPER_ADMIN"
                }

                if (!hasRole) {
                    throw AccessDeniedException("You are not authorized to subscribe to this topic.")
                }
            } else {
                throw AccessDeniedException("Unauthenticated WebSocket subscription attempt.")
            }
        }

        return message
    }
}
