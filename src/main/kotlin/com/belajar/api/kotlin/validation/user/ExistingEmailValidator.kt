package com.belajar.api.kotlin.validation.user

import com.belajar.api.kotlin.annotation.user.ExistingEmail
import com.belajar.api.kotlin.repository.UserRepository
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.factory.annotation.Autowired

class ExistingEmailValidator(
    private var userRepository: UserRepository
) : ConstraintValidator<ExistingEmail, String> {

    override fun initialize(constraintAnnotation: ExistingEmail) {
        super.initialize(constraintAnnotation)
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) {
            return false
        }
        return userRepository.existsByEmail(value)
    }
}
