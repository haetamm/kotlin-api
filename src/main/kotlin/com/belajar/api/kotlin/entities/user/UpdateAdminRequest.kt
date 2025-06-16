package com.belajar.api.kotlin.entities.user

import com.belajar.api.kotlin.annotation.user.PassIfNotBlank
import com.belajar.api.kotlin.annotation.user.UniqueEmail
import com.belajar.api.kotlin.annotation.user.UniqueUsername
import com.belajar.api.kotlin.annotation.user.ValidEmail
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateAdminRequest (

    @field:NotBlank
    @field:ValidEmail
    val email: String?,

    @field:NotBlank
    @field:Pattern(regexp = "^[a-zA-Z0-9]+\$", message = "must contain only alphanumeric characters")
    @field:Size(min = 3, max = 8)
    val username: String?,

    @field:PassIfNotBlank
    val password: String?
)