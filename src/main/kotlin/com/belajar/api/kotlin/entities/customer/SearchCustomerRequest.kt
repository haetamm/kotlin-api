package com.belajar.api.kotlin.entities.customer

data class SearchCustomerRequest(
    val name: String? = null,
    val direction: String = "desc",
    val sortBy: String = "name",
    val page: Int = 1,
    val size: Int = 10,
)
