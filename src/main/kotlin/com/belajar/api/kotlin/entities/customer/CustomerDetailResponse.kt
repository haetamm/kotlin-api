package com.belajar.api.kotlin.entities.customer

data class CustomerDetailResponse(
    var id: String,
    val name: String,
    val phoneNumber: String,
    val member: Boolean,
    val address: String?,
    var history: List<CustomerTransactionResponse>
)
