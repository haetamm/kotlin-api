package com.belajar.api.kotlin.entities.bill

import com.belajar.api.kotlin.constant.TransTypeEnum
import com.fasterxml.jackson.annotation.JsonFormat

data class SearchBillRequest(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val from: String? = null,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val to: String? = null,

    val customerName: String? = null,
    val transType: TransTypeEnum? = null,
    val transactionStatus: String? = null,
    val direction: String = "desc",
    val sortBy: String = "transDate",
    val page: Int = 1,
    val size: Int = 10
)
