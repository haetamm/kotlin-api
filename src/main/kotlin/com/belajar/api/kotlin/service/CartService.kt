package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.entities.cart.CartRequest
import com.belajar.api.kotlin.entities.cart.DeleteCartItemRequest
import com.belajar.api.kotlin.entities.cart_item.CartItemResponse
import com.belajar.api.kotlin.model.Cart

interface CartService {
    fun save(request: CartRequest): List<CartItemResponse>
    fun getAll(): List<CartItemResponse>
    fun deleteByMenuId(request: DeleteCartItemRequest): String
    fun findByCustomerId(id: String): Cart
}