package com.belajar.api.kotlin.websocket

import com.belajar.api.kotlin.model.UserAccount
import org.springframework.http.server.ServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

class CustomHandshakeHandler : DefaultHandshakeHandler() {
    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Principal? {
        val username = attributes["username"] as? String
        val user = attributes["user"] as? UserAccount

        return if (username != null && user != null) {
            CustomPrincipal(username, user)
        } else {
            null
        }
    }
}
