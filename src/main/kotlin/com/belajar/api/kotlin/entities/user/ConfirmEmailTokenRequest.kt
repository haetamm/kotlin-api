package com.belajar.api.kotlin.entities.user

import jakarta.validation.constraints.NotBlank

data class ConfirmEmailTokenRequest (

    @field:NotBlank
    val confirmationEmailToken: String

)
