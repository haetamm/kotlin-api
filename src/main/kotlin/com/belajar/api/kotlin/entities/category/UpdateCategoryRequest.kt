package com.belajar.api.kotlin.entities.category

import jakarta.validation.constraints.NotBlank

data class UpdateCategoryRequest (
    @field:NotBlank
    var name: String
)