package com.belajar.api.kotlin.entities.user

import com.belajar.api.kotlin.annotation.user.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateCurrentUserRequest (

    @field:NotBlank
    @field:Pattern(regexp = "^[a-zA-Z ]+\$", message = "must contain only alphabet characters and spaces")
    @field:Size(min = 4, max = 23)
    val name: String?,

    @field:NotBlank
    @field:Pattern(regexp = "^[0-9 ]+\$", message = "must contain only numeric characters and spaces")
    @field:Size(max = 25)
    val phone: String?,

    @field:NotBlank
    @field:Size(max = 225)
    val address: String?,

    @field:UsernameIfNotBlank
    val username: String?,

)
