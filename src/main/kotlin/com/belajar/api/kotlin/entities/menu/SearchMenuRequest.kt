package com.belajar.api.kotlin.entities.menu

data class SearchMenuRequest(
    val category: String,
    val name: String,
    val minPrice: Long,
    val maxPrice: Long,
    val direction: String,
    val sortBy: String,
    val page: Int,
    val size: Int,
)
