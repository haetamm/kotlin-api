package com.belajar.api.kotlin.entities.menu

import com.belajar.api.kotlin.annotation.menu.UniqueNameMenu
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class MenuRequest(
    @field:NotBlank
    @field:UniqueNameMenu
    var name: String,

    @field:NotNull
    var price: Long,

    @field:NotBlank
    var categoryId: String
)
