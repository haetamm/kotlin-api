package com.belajar.api.kotlin.entities.table

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class TableRequest(
    @field:NotBlank(message = "Name cannot be blank")
    @field:Size(max = 50, message = "Name must be less than 50 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9 ]+$",
        message = "Name must contain only letters and spaces"
    )
    val name: String,

    @field:NotNull(message = "isTaken cannot be null")
    val isTaken: Boolean
)