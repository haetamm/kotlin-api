package com.belajar.api.kotlin.annotation.category

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.validation.category.UniqueNameCategoryValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [UniqueNameCategoryValidator::class])
annotation class UniqueNameCategory(
    val message: String = StatusMessage.NAME_CATEGORY_BEEN_TAKEN,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
