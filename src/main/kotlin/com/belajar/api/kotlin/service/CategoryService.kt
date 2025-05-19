package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.entities.category.CategoryRequest
import com.belajar.api.kotlin.entities.category.CategoryResponse
import com.belajar.api.kotlin.entities.category.UpdateCategoryRequest
import com.belajar.api.kotlin.model.Category

interface CategoryService {
    fun getById(id: String): Category
    fun save(categoryRequest: CategoryRequest): CategoryResponse
    fun getAll(): List<CategoryResponse>
    fun update(categoryRequest: UpdateCategoryRequest, id: String): CategoryResponse
    fun delete(id: String): String
}