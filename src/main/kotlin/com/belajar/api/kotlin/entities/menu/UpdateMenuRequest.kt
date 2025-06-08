package com.belajar.api.kotlin.entities.menu

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UpdateMenuRequest(
    @field:NotBlank
    var name: String,

    @field:NotNull
    var price: Long,

    @field:NotBlank
    var categoryId: String
)
