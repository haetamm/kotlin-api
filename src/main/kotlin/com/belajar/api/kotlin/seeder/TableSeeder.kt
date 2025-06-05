package com.belajar.api.kotlin.seeder

import com.belajar.api.kotlin.model.TableRest
import com.belajar.api.kotlin.repository.TableRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Order(3)
@Component
class TableSeeder(
    private val tableRepository: TableRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val names = listOf("A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10",
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10", "Mang Gari")
        val existingNames = tableRepository.findAll().map { it.name }

        val newTables = names
            .filterNot { existingNames.contains(it) }
            .map { TableRest(name = it, isTaken = false) } // Tambahkan isTaken = false

        if (newTables.isNotEmpty()) {
            tableRepository.saveAll(newTables)
            println("✅ Table seeding complete.")
        } else {
            println("ℹ️ Table data already seeded.")
        }
    }
}