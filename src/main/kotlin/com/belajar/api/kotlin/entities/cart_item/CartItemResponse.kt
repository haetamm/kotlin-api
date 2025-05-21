package com.belajar.api.kotlin.entities.cart_item

data class CartItemResponse (
    var id: String,
    var menuId: String,
    var name: String,
    var image: String?,
    var qty: Int,
    var price: Long
)