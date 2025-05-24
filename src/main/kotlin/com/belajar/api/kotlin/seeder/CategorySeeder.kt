package com.belajar.api.kotlin.seeder

import com.belajar.api.kotlin.model.Category
import com.belajar.api.kotlin.repository.CategoryRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Order(1)
@Component
class CategorySeeder(
    private val categoryRepository: CategoryRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        if (categoryRepository.count() == 0L) {
            val categories = listOf(
                Category(name = "main"),
                Category(name = "fried"),
                Category(name = "soup"),
                Category(name = "drink")
            )

            categoryRepository.saveAll(categories)
            println("✅ Category seeding complete.")
        }
    }
}
