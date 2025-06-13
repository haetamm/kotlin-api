package com.belajar.api.kotlin.repository

import com.belajar.api.kotlin.model.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface CartItemRepository: JpaRepository<CartItem, String> {
    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.menu.id = :menuId")
    fun deleteByMenuId(menuId: String)
}