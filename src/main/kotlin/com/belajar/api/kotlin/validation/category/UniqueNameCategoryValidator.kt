package com.belajar.api.kotlin.validation.category

import com.belajar.api.kotlin.annotation.category.UniqueNameCategory
import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.repository.CategoryRepository
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Component

@Component
class UniqueNameCategoryValidator(
    private val categoryRepository: CategoryRepository
) : ConstraintValidator<UniqueNameCategory, String> {

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
            val exists = categoryRepository.existsByNameIncludingDeleted(value)
            if (exists) {
                context?.disableDefaultConstraintViolation()
                context?.buildConstraintViolationWithTemplate(StatusMessage.NAME_CATEGORY_BEEN_TAKEN)
                    ?.addConstraintViolation()
                return false
            }
            return true
    }
}