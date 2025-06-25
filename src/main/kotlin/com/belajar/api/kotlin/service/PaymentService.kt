package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.entities.payment.MidtransWebhookRequest
import com.belajar.api.kotlin.model.Bill
import com.belajar.api.kotlin.model.Payment

interface PaymentService {
    fun createPayment(bill: Bill): Payment
    fun handleWebhook(request: MidtransWebhookRequest): String
    fun  syncPaymentStatus(orderId: String): String
}