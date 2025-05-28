package com.belajar.api.kotlin.specification

import com.belajar.api.kotlin.entities.bill.SearchBillRequest
import com.belajar.api.kotlin.model.*
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
                val start = java.sql.Date(fromDate.time)
                predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.function("DATE", java.sql.Date::class.java, root.get<Timestamp>("transDate")),
                        criteriaBuilder.literal(start)
                    )
                )
            }

            request.to?.let {
                val toDate = utilities.parseDate(it, "yyyy-MM-dd")
                val end = java.sql.Date(toDate.time)
                predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.function("DATE", java.sql.Date::class.java, root.get<Timestamp>("transDate")),
                        criteriaBuilder.literal(end)
                    )
                )
            }

            // Filter by customer name
            request.customerName?.takeIf { it.isNotBlank() }?.let {
                val customerJoin = root.join<Bill, Customer>("customer")
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%${it.lowercase()}%"))
            }

            // Filter by menu name
            request.menuName?.takeIf { it.isNotBlank() }?.let {
                val billDetailJoin = root.join<Bill, BillDetail>("billDetails")
                val menuJoin = billDetailJoin.join<BillDetail, Menu>("menu")
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(menuJoin.get("name")), "%${it.lowercase()}%"))
            }

            // Filter by transType
            request.transType?.let {
                predicates.add(criteriaBuilder.equal(root.get<TransType>("transType").get<Any>("id"), it))
            }

            // Filter by transactionStatus
            request.transactionStatus?.takeIf{ it.isNotBlank() }?.let {
                val paymentJoin = root.join<Bill, Payment>("payment")
                predicates.add(criteriaBuilder.equal(paymentJoin.get<String>("transactionStatus"), it))
            }

            // Combine predicates
            query.where(*predicates.toTypedArray())
            query.restriction
        }
    }
}