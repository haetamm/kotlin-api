package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.category.CategoryRequest
import com.belajar.api.kotlin.entities.category.CategoryResponse
import com.belajar.api.kotlin.entities.category.UpdateCategoryRequest
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.exception.ValidationCustomException
import com.belajar.api.kotlin.model.Category
import com.belajar.api.kotlin.repository.CategoryRepository
import com.belajar.api.kotlin.service.CategoryService
import com.belajar.api.kotlin.validation.ValidationUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class CategoryServiceImpl (private val categoryRepository: CategoryRepository, private val validationUtil: ValidationUtil): CategoryService {
    @Transactional(rollbackFor = [Exception::class])
    override fun getById(id: String): Category {
        val category = findById(id)
        return category
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun save(categoryRequest: CategoryRequest): CategoryResponse {
        validationUtil.validate(categoryRequest)

        val category = categoryRepository.saveAndFlush(
            Category(name = categoryRequest.name)
        )

        return CategoryResponse(id = category.id!!, name = category.name)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAll(): List<CategoryResponse> {
        val categories = categoryRepository.findAll()
        return categories.map{category -> CategoryResponse(id = category.id!!, name = category.name)}
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun update(categoryRequest: UpdateCategoryRequest, id: String): CategoryResponse {
        validationUtil.validate(categoryRequest)
        val category = findById(id)

        if (category.name != categoryRequest.name) {
            val existingCategory = categoryRepository.existsByNameIncludingDeleted(categoryRequest.name)
            if (existingCategory) {
                throw ValidationCustomException(StatusMessage.NAME_CATEGORY_BEEN_TAKEN, "name")
            }
        }

        category.name = categoryRequest.name
        categoryRepository.saveAndFlush(category)
        return CategoryResponse(id = category.id!!, name = category.name)
    }


    @Transactional(rollbackFor = [Exception::class])
    override fun delete(id: String): String {
        val category = findById(id)
        categoryRepository.softDelete(category.id.toString())
        return StatusMessage.SUCCESS_DELETE
    }

    private fun findById(id: String): Category {
        return categoryRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.CATEGORY_NOT_FOUND)
        }
    }
}