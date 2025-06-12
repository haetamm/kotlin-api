package com.belajar.api.kotlin.entities.customer

data class CustomerResponse(
    var id: String,
    val name: String,
    val phoneNumber: String,
    val member: Boolean,
    val address: String?,
)
