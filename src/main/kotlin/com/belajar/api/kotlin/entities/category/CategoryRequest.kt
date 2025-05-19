package com.belajar.api.kotlin.entities.category

import com.belajar.api.kotlin.annotation.category.UniqueNameCategory
import jakarta.validation.constraints.NotBlank

data class CategoryRequest (
    @field:NotBlank
    @field:UniqueNameCategory
    var name: String
)