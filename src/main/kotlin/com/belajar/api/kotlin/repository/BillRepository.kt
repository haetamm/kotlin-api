package com.belajar.api.kotlin.repository

import com.belajar.api.kotlin.model.Bill
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface BillRepository: JpaRepository<Bill, String>, JpaSpecificationExecutor<Bill> {
    fun findByCustomerId(customerId: String): List<Bill>

}