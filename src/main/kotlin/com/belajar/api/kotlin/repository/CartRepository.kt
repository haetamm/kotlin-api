package com.belajar.api.kotlin.repository

import com.belajar.api.kotlin.model.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CartRepository: JpaRepository<Cart, String> {
    @Query("SELECT c FROM Cart c WHERE c.customer.id = :customerId")
    fun findByCustomerId(customerId: String): List<Cart>
}