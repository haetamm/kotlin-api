package com.belajar.api.kotlin.config

import com.belajar.api.kotlin.repository.UserAccountRepository
import com.belajar.api.kotlin.service.MyUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.client.RestTemplate


@Configuration
class BeanConfiguration  {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(myAuthenticationProviders: List<AuthenticationProvider?>?): AuthenticationManager {
        return ProviderManager(myAuthenticationProviders)
    }

    @Bean
    fun authenticationProvider(userDetailsService: UserDetailsService, passwordEncoder: PasswordEncoder): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder)
        return authProvider
    }

    fun myUserDetailsService(userAccountRepository: UserAccountRepository): UserDetailsService {
        return MyUserDetailsService(userAccountRepository) // Implementasi custom Anda
    }


//    @Bean
//    fun objectMapper(): ObjectMapper = ObjectMapper()

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

}