package com.belajar.api.kotlin.entities.bill

import com.belajar.api.kotlin.entities.bill_detail.BillDetailResponse
import com.belajar.api.kotlin.entities.payment.PaymentResponse

data class BillResponse(
    var id: String,
    var recipientName: String?,
    var phone: String?,
    var deliveryAddress: String?,
    var transDate: String,
    var customerId: String,
    var customerName: String,
    var tableName: String?,
    var transType: String,
    var billDetails: List<BillDetailResponse>,
    var payment: PaymentResponse
)
