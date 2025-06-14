package com.belajar.api.kotlin.security

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.exception.UnauthorizedException
import com.belajar.api.kotlin.service.JwtService
import com.belajar.api.kotlin.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Configuration

class AuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserService
) : OncePerRequestFilter() {

    private val AUTH_HEADER = "Authorization"

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val bearerToken = request.getHeader(AUTH_HEADER)

            if (bearerToken != null && jwtService.verifyJwtToken(bearerToken)) {
                val jwtClaims = jwtService.getClaimsByToken(bearerToken)
                val userAccount = userService.getUserById(jwtClaims.userAccountId!!.toInt())

                if (!userAccount.isEnable) {
                    throw UnauthorizedException(StatusMessage.ACCESS_DENIED)
                }

                val authentication = UsernamePasswordAuthenticationToken(
                    userAccount,
                    null,
                    userAccount.authorities
                )
                authentication.details = WebAuthenticationDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: UnauthorizedException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
            return
        }

        filterChain.doFilter(request, response)
    }

}