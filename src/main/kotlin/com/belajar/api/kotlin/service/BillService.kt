package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.entities.bill.*
import com.belajar.api.kotlin.model.Bill
import org.springframework.data.domain.Page


interface BillService {
    fun createDineInBill(request: DineInBillRequest): BillResponse
    fun createDeliveryBill(request: DeliveryBillRequest): BillResponse
    fun getById(id: String): BillResponse
    fun currentUserGetById(id: String): BillResponse
    fun getAll(request: SearchBillRequest): Page<BillResponse>
    fun getByCurrentUser(request: SearchBillRequest): Page<BillResponse>
    fun updateStatusPayment(request: UpdateBillRequest, id: String): String
    fun findByCustomerId(customerId: String): List<Bill>

}