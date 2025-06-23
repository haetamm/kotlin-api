package com.belajar.api.kotlin.websocket

import com.belajar.api.kotlin.exception.UnauthorizedException
import com.belajar.api.kotlin.service.JwtService
import com.belajar.api.kotlin.service.UserService
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class CustomHandshakeInterceptor(
    private val jwtService: JwtService,
    private val userService: UserService
) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val servletRequest = (request as ServletServerHttpRequest).servletRequest
        val token = servletRequest.getParameter("token")

        if (token != null && jwtService.verifyJwtToken(token)) {
            val claims = jwtService.getClaimsByToken(token)
            val user = userService.getUserById(claims.userAccountId!!.toInt())

            if (!user.isEnable) throw UnauthorizedException("Account disabled")

            attributes["user"] = user
            attributes["username"] = user.username // penting!
            return true
        }

        return false
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {}
}
