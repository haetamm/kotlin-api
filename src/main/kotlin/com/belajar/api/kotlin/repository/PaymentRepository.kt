package com.belajar.api.kotlin.repository

import com.belajar.api.kotlin.model.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository: JpaRepository<Payment, String>