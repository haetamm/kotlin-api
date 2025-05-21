package com.belajar.api.kotlin.entities.cart_item

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CartItemRequest(
    @field:NotBlank
    val menuId: String,

    @field:Min(value = 1, message = "Quantity must be at least 1")
    val qty: Int
)