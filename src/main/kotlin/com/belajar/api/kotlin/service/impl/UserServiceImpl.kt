package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.constant.UserRoleEnum
import com.belajar.api.kotlin.entities.user.RegisterRequest
import com.belajar.api.kotlin.entities.user.UpdateUserCurrentRequest
import com.belajar.api.kotlin.entities.user.UserResponse
import com.belajar.api.kotlin.exception.ForbiddenException
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.exception.ValidationCustomException
import com.belajar.api.kotlin.model.UserAccount
import com.belajar.api.kotlin.repository.UserAccountRepository
import com.belajar.api.kotlin.service.JwtService
import com.belajar.api.kotlin.service.UserService
import com.belajar.api.kotlin.validation.ValidationUtil
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class UserServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    val validationUtil: ValidationUtil,
    val authenticationManager: AuthenticationManager,
    val jwtService: JwtService,
    val passwordEncoder: PasswordEncoder
): UserService {

    override fun getUserById(id: Int): UserAccount {
        return findById(id)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getUserByUsername(username: String): UserResponse<String> {
        val user = userAccountRepository.findByUsername(username).orElseThrow {
            throw NotFoundException(StatusMessage.USER_NOT_FOUND)
        }
        return createUserResponse(user, "null")
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getUserCurrent(): UserResponse<String> {
        val userId = SecurityContextHolder.getContext().authentication.name
        val user = findById(userId.toInt())
        return createUserResponse(user, "null")
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateUserCurrent(updateUserCurrentRequest: UpdateUserCurrentRequest): UserResponse<String> {
        validationUtil.validate(updateUserCurrentRequest)
        val userId = SecurityContextHolder.getContext().authentication.name
        val user = findById(userId.toInt())

        if (!user.comparePassword(updateUserCurrentRequest.passwordConfirmation!!)) {
            throw ValidationCustomException("Password incorrect", "password")
        }

        updateUserData(updateUserCurrentRequest, user)
        userAccountRepository.save(user)

        val newAuthentication = UsernamePasswordAuthenticationToken(
            user.username,
            updateUserCurrentRequest.passwordConfirmation
        )
        SecurityContextHolder.getContext().authentication = authenticationManager.authenticate(newAuthentication)
        val token = jwtService.generateToken(user)
        return createUserResponse(user, token)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun disabledOrEnabledUserById(id: Int): String {
        val user = findById(id)

        if (user.roles.any { it.role == UserRoleEnum.ROLE_SUPER_ADMIN }) {
            throw ForbiddenException(StatusMessage.ACCESS_DENIED)
        }

        if (user.isEnable) {
            user.isEnable = false
            userAccountRepository.save(user)
            return StatusMessage.SUCCESS_DISABLED
        } else {
            user.isEnable = true
            userAccountRepository.save(user)
            return StatusMessage.SUCCESS_ENABLED
        }

    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getUserAll(): List<UserResponse<String>> {
        val response = userAccountRepository.findAll();
        val users = response
            .filter { user ->
                user.roles.size == 1 && user.roles.any { role -> role.role == UserRoleEnum.ROLE_USER }}
            .map { createUserResponse(it, "null") }
        return users
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAdminAll(): List<UserResponse<String>> {
        val response = userAccountRepository.findAll();
        val users = response
            .filter { user ->
                user.roles.size == 2 && user.roles.any { role -> role.role == UserRoleEnum.ROLE_ADMIN }}
            .map { createUserResponse(it, "null") }
        return users
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateAdminById(id: Int, request: RegisterRequest): UserResponse<String> {
        validationUtil.validate(request)
        val user = findById(id)

        if (user.roles.any { it.role == UserRoleEnum.ROLE_SUPER_ADMIN } || user.roles.size == 1 && user.roles.any { it.role == UserRoleEnum.ROLE_USER }) {
            throw ForbiddenException(StatusMessage.ACCESS_DENIED)
        }

        user.email = request.email!!
        user.updateUsername(request.username!!)
        user.updatePassword(passwordEncoder.encode(request.password))

        userAccountRepository.save(user)
        return createUserResponse(user, "null")
    }

     fun findById(id: Int): UserAccount {
        return userAccountRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.USER_NOT_FOUND)
        }
    }

    private fun updateUserData(updateUserCurrentRequest: UpdateUserCurrentRequest, user: UserAccount) {
        updateUsernameIfChange(updateUserCurrentRequest.username!!, user)
        updateEmailIfChange(updateUserCurrentRequest.email!!, user)
    }

    private fun updateUsernameIfChange(newUsername: String, user: UserAccount) {
        if (newUsername.isNotBlank() && newUsername != user.username) {
            if (userAccountRepository.existsByUsername(newUsername)) {
                throw ValidationCustomException(StatusMessage.USERNAME_BEEN_TAKEN, "username")
            }
            user.updateUsername(newUsername)
        }
    }

    private fun updateEmailIfChange(newEmail: String, user: UserAccount) {
        if (newEmail.isNotBlank() && newEmail != user.email) {
            if (userAccountRepository.existsByEmail(newEmail)) {
                throw ValidationCustomException(StatusMessage.EMAIL_TAKEN, "email")
            }
            user.email = newEmail
        }
    }

    private fun createUserResponse(user: UserAccount, token: String): UserResponse<String> {
        val roleNames = user.roles.map { it.role }
        return UserResponse(
            id = user.id!!,
            email = user.email,
            username = user.username,
            roles = roleNames,
            createdAt = user.createdAt.toString(),
            updatedAt = user.updatedAt.toString(),
            token = token
        )
    }

}