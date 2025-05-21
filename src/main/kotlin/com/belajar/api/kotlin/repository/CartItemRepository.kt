package com.belajar.api.kotlin.repository

import com.belajar.api.kotlin.model.CartItem
import org.springframework.data.jpa.repository.JpaRepository

interface CartItemRepository: JpaRepository<CartItem, String> {
}