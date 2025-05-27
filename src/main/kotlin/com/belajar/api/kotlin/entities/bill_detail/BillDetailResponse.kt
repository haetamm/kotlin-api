package com.belajar.api.kotlin.entities.bill_detail

data class BillDetailResponse(
    var id: String,
    var menuId: String,
    var name: String,
    var qty: Int,
    var price: Long
)
