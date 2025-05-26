package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.entities.bill.*
import org.springframework.data.domain.Page


interface BillService {
    fun createDineInBill(request: DineInBillRequest): BillResponse
    fun createDeliveryBill(request: DeliveryBillRequest): BillResponse
    fun getById(id: String): BillResponse
    fun getAll(request: SearchBillRequest): Page<BillResponse>
    fun updateStatusPayment(request: UpdateBillRequest, id: String): String
}