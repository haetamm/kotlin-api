package com.belajar.api.kotlin.specification

import com.belajar.api.kotlin.entities.menu.SearchMenuRequest
import com.belajar.api.kotlin.model.Menu
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.domain.Specification
import jakarta.persistence.criteria.Predicate

@Configuration
class MenuSpecification {
    fun specification(request: SearchMenuRequest): Specification<Menu> {
        return Specification<Menu> { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            // filter untuk deleted = false
            predicates.add(criteriaBuilder.equal(root.get<Boolean>("deleted"), false))

            if (request.category.isNotBlank() && request.category != "all") {
                val categoryFilter = criteriaBuilder.equal(
                    root.get<Menu>("category").get<String>("name"), // Akses category.name
                    request.category
                )
                predicates.add(categoryFilter)
            }

            // Filter nama hanya diterapkan jika name bukan "all" atau kosong
            if (request.name.isNotBlank() && request.name != "all") {
                val nameFilter = criteriaBuilder.like(
                    criteriaBuilder.upper(root.get("name")),
                    "%${request.name.uppercase()}%"
                )
                predicates.add(nameFilter)
            }

            // Filter harga hanya diterapkan jika minPrice dan maxPrice memiliki nilai valid
            val priceFilter = criteriaBuilder.between(
                root.get("price"),
                request.minPrice,
                request.maxPrice
            )
            predicates.add(priceFilter)

            query.where(*predicates.toTypedArray()).restriction
        }
    }
}