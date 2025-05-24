package com.belajar.api.kotlin.seeder

import com.belajar.api.kotlin.model.Menu
import com.belajar.api.kotlin.repository.CategoryRepository
import com.belajar.api.kotlin.repository.MenuRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Order(2)
@Component
class MenuSeeder(
    private val menuRepository: MenuRepository,
    private val categoryRepository: CategoryRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        if (menuRepository.count() > 0) return

        val categories = categoryRepository.findAll().associateBy { it.name }

        val menus = listOf(
            "Nasi Goreng" to "main",
            "Mie Ayam" to "main",
            "Ayam Geprek" to "fried",
            "Bakso" to "soup",
            "Soto Ayam" to "soup",
            "Es Teh Manis" to "drink",
            "Es Jeruk" to "drink",
            "Teh Tarik" to "drink",
            "Ayam Goreng" to "fried",
            "Lele Goreng" to "fried",
            "Capcay" to "main",
            "Nasi Uduk" to "main",
            "Sup Buntut" to "soup",
            "Sayur Asem" to "soup",
            "Es Campur" to "drink",
            "Kopi Susu" to "drink",
            "Ikan Bakar" to "main",
            "Tempe Mendoan" to "fried",
            "Tahu Crispy" to "fried",
            "Sop Iga" to "soup"
        )

        val seededMenus = menus.map { (name, categoryKey) ->
            val category = categories[categoryKey]
                ?: throw IllegalStateException("Category '$categoryKey' not found")

            Menu(
                name = name,
                price = Random.nextLong(5000, 25000),
                category = category
            )
        }

        menuRepository.saveAll(seededMenus)
        println("✅ Menu seeding complete with 20 items.")
    }
}
