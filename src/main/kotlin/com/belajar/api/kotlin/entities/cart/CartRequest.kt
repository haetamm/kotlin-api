package com.belajar.api.kotlin.entities.cart

import com.belajar.api.kotlin.entities.cart_item.CartItemRequest
import jakarta.validation.constraints.NotEmpty

data class CartRequest (
    @NotEmpty(message = "Menu item cannot be empty")
    var menuRequest: List<CartItemRequest>
)
