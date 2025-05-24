package com.belajar.api.kotlin.entities.bill_detail

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BillDetailRequest(
    @field:NotBlank
    var menuId: String,

    @field:NotNull
    @field:Min(value = 1, message = "Quantity must be at least 1")
    var qty: Int,
)
