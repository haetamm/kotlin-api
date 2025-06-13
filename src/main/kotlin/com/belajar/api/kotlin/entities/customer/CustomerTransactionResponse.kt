package com.belajar.api.kotlin.entities.customer
data class CustomerTransactionResponse(
    var billId: String,
    var transDate: String,
    var transactionStatus: String,
    var totalPayment: Long
)
