package com.belajar.api.kotlin.entities.user

import com.belajar.api.kotlin.constant.UserRoleEnum
data class UserResponse <T>(
    val id: String,
    val name: String?,
    val phone: String?,
    val address: String?,
    val email: String,
    val username: String,
    val isEnable: Boolean,
    val roles: List<UserRoleEnum?>,
    val createdAt: String,
    val updatedAt: String,
)
