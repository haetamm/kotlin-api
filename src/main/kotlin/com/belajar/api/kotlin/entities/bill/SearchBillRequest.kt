package com.belajar.api.kotlin.entities.bill

import com.belajar.api.kotlin.constant.TransTypeEnum

data class SearchBillRequest(
    val from: String? = null, // Start date for transDate (format: yyyy-MM-dd)
    val to: String? = null,   // End date for transDate (format: yyyy-MM-dd)
    val customerName: String? = null, // Filter for customer name
    val transType: TransTypeEnum? = null, // Filter for transaction type
    val transactionStatus: String? = null, // Filter for payment transaction status
    val direction: String = "asc",
    val sortBy: String = "transDate",
    val page: Int = 1,
    val size: Int = 10
)