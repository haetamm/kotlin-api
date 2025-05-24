package com.belajar.api.kotlin.entities.bill

import com.belajar.api.kotlin.entities.bill_detail.BillDetailRequest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class DeliveryBillRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^[a-zA-Z ]+\$", message = "must contain only alphabet characters and spaces")
    @field:Size(min = 4, max = 23)
    val recipientName: String,

    @field:NotBlank
    @field:Pattern(regexp = "^[0-9 ]+\$", message = "must contain only numeric characters and spaces")
    @field:Size(max = 25)
    val phone: String,

    @field:NotBlank
    @field:Size(max = 225)
    val deliveryAddress: String,

    @NotEmpty(message = "billRequest cannot be empty")
    var billRequest: List<BillDetailRequest>
)
