package com.belajar.api.kotlin.annotation.user

import com.belajar.api.kotlin.validation.user.PassIfNotBlankValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [PassIfNotBlankValidator::class])
annotation class PassIfNotBlank(
    val message: String = "Password must contain only alphanumeric characters and size must be between 4 and 6",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
