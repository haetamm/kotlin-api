package com.belajar.api.kotlin.entities.cart

import com.belajar.api.kotlin.entities.cart_item.ItemRequest
import jakarta.validation.constraints.NotEmpty

data class DeleteCartItemRequest (
    @NotEmpty(message = "Menu item cannot be empty")
    var items: List<ItemRequest>
)