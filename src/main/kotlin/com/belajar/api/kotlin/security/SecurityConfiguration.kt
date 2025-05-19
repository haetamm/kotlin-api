package com.belajar.api.kotlin.security

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val authenticationFilter: AuthenticationFilter,
) {

    @Bean
    fun filterChain(security: HttpSecurity): SecurityFilterChain {
        return security
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auth/confirm/**").permitAll()
                    .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/menus/*/images/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

}