package com.belajar.api.kotlin.entities.bill

import com.belajar.api.kotlin.annotation.table.ValidTableName
import com.belajar.api.kotlin.annotation.trans_type.ValidTransTypeEnum
import com.belajar.api.kotlin.entities.bill_detail.BillDetailRequest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class DineInBillRequest(
    @field:NotBlank
    var customerName: String,

    var customerPhone: String,

    @field:NotBlank
    @field:ValidTableName
    var tableName: String,

    @NotEmpty(message = "billRequest cannot be empty")
    var billRequest: List<BillDetailRequest>
)
