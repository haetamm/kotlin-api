package com.belajar.api.kotlin.entities.user

import com.belajar.api.kotlin.annotation.user.*
import jakarta.validation.constraints.NotBlank


data class UpdateCurrentUserEmailRequest (

    @field:NotBlank
    @field:ValidEmail
    @field:UniqueEmail
    val newEmail: String,

    @field:NotBlank
    val passwordConfirmation: String

)
