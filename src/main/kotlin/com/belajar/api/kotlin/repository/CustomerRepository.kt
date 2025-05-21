package com.belajar.api.kotlin.repository

import com.belajar.api.kotlin.model.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CustomerRepository : JpaRepository<Customer, String>, JpaSpecificationExecutor<Customer> {
    fun findByNameLikeIgnoreCaseAndPhoneEquals(name: String, phone: String): Optional<Customer>

    @Modifying
    @Query("update Customer c set c.deleted = true where c.id = :id")
    fun softDelete(@Param("id") id: String)

    fun findByUserAccountId(userAccountId: Int): Customer?
}