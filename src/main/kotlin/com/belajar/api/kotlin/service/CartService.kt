package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.entities.cart.CartRequest
import com.belajar.api.kotlin.entities.cart_item.CartItemResponse

interface CartService {
    fun save(request: CartRequest): List<CartItemResponse>
    fun getAll(): List<CartItemResponse>
}