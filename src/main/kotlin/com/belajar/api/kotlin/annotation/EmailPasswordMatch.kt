package com.belajar.api.kotlin.annotation

import com.belajar.api.kotlin.validation.EmailPasswordMatchValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [EmailPasswordMatchValidator::class])
annotation class EmailPasswordMatch(
    val message: String = "Invalid password",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
