package com.belajar.api.kotlin.specification

import com.belajar.api.kotlin.entities.bill.SearchBillRequest
import com.belajar.api.kotlin.model.Bill
import com.belajar.api.kotlin.model.Customer
import com.belajar.api.kotlin.model.Payment
import com.belajar.api.kotlin.model.TransType
import com.belajar.api.kotlin.utils.Utilities
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.domain.Specification
import java.sql.Timestamp

@Configuration
class BillSpecification(
    private val utilities: Utilities
) {
    fun specification(request: SearchBillRequest): Specification<Bill> {
        return Specification { root: Root<Bill>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            // Filter by transDate range (from and to)
            request.from?.let {
                val fromDate = utilities.parseDate(it, "yyyy-MM-dd")
                val start = Timestamp(fromDate.time)
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transDate"), start))
            }

            request.to?.let {
                val toDate = utilities.parseDate(it, "yyyy-MM-dd")
                val end = Timestamp(toDate.time).apply { nanos = 999999999 } // Set to end of day
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transDate"), end))
            }

            // Filter by customer name
            request.customerName?.let {
                val customerJoin = root.join<Bill, Customer>("customer")
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%${it.lowercase()}%"))
            }

            // Filter by transType
            request.transType?.let {
                predicates.add(criteriaBuilder.equal(root.get<TransType>("transType").get<Any>("id"), it))
            }

            // Filter by transactionStatus
            request.transactionStatus?.let {
                val paymentJoin = root.join<Bill, Payment>("payment")
                predicates.add(criteriaBuilder.equal(paymentJoin.get<String>("transactionStatus"), it))
            }

            // Combine predicates
            query.where(*predicates.toTypedArray())
            query.restriction
        }
    }
}