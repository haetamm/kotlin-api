package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.entities.user.*
import com.belajar.api.kotlin.model.UserAccount

interface UserService {
    fun getUserById(id: Int): UserAccount
    fun getUserByUsername(username: String): UserResponse<String>
    fun getCurrentUser(): UserResponse<String>
    fun updateCurrentUser(updateCurrentUserRequest: UpdateCurrentUserRequest): UserResponse<String>
    fun updateCurrentUserEmail(updateCurrentUserEmailRequest: UpdateCurrentUserEmailRequest): String
    fun updateCurrentUserPassword(updateCurrentUserPasswordRequest: UpdateCurrentUserPasswordRequest): String
    fun emailConfirmation(confirmEmailTokenRequest: ConfirmEmailTokenRequest): UserResponse<String>
    fun disabledOrEnabledUserById(id: String): String
    fun getUserAll(): List<UserResponse<String>>
    fun getAdminAll(): List<UserResponse<String>>
    fun updateAdminById(id: Int, request: RegisterRequest): UserResponse<String>
}