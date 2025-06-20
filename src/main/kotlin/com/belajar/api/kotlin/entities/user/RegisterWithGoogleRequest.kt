package com.belajar.api.kotlin.entities.user

import com.belajar.api.kotlin.annotation.user.PassIfNotBlank
import com.belajar.api.kotlin.annotation.user.UniqueEmail
import com.belajar.api.kotlin.annotation.user.UniqueUsername
import com.belajar.api.kotlin.annotation.user.ValidEmail
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterWithGoogleRequest(

    @field:NotBlank(message = "Email is required")
    @field:ValidEmail
    @field:UniqueEmail
    val email: String?,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 4, max = 23, message = "Name must be between 4 and 23 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z ]+$",
        message = "Name must contain only alphabet characters and spaces"
    )
    val name: String?,

    @field:NotBlank(message = "Phone is required")
    @field:Size(max = 25, message = "Phone must not exceed 25 characters")
    @field:Pattern(
        regexp = "^[0-9 ]+$",
        message = "Phone must contain only numeric characters and spaces"
    )
    val phone: String?,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 8, message = "Username must be between 3 and 8 characters")
    @field:UniqueUsername
    @field:Pattern(
        regexp = "^[a-zA-Z0-9]+$",
        message = "Username must contain only alphanumeric characters"
    )
    val username: String?,

    @PassIfNotBlank
    val password: String?,

    @field:NotBlank(message = "Google token is required")
    val tokenAccess: String?
)
