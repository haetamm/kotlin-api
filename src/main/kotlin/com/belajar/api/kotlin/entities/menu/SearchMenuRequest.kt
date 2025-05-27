package com.belajar.api.kotlin.entities.menu

data class SearchMenuRequest(
    val category: String = "all",
    val name: String = "all",
    val minPrice: Long = 0,
    val maxPrice: Long = 100000000,
    val direction: String = "asc",
    val sortBy: String = "name",
    val page: Int = 1,
    val size: Int = 10
)
