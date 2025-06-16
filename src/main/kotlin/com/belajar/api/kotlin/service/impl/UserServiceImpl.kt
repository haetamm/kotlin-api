package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.constant.UserRoleEnum
import com.belajar.api.kotlin.entities.user.*
import com.belajar.api.kotlin.exception.ForbiddenException
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.exception.ValidationCustomException
import com.belajar.api.kotlin.model.UserAccount
import com.belajar.api.kotlin.repository.UserAccountRepository
import com.belajar.api.kotlin.service.CustomerService
import com.belajar.api.kotlin.service.UserService
import com.belajar.api.kotlin.utils.Utilities
import com.belajar.api.kotlin.validation.ValidationUtil
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*



@Service
class UserServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    val validationUtil: ValidationUtil,
    val passwordEncoder: PasswordEncoder,
    val customerService: CustomerService,
    val mailSender: JavaMailSender,
    val utilities: Utilities,
): UserService {

    @Transactional(rollbackFor = [Exception::class])
    override fun getUserById(id: Int): UserAccount {
        return findById(id)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getCurrentUser(): UserResponse<String> {
        val userId = utilities.getUserId()
        val user = findById(userId)
        return createUserResponse(user)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateCurrentUser(updateCurrentUserRequest: UpdateCurrentUserRequest): UserResponse<String> {
        validationUtil.validate(updateCurrentUserRequest)
        val userId = utilities.getUserId()
        val user = findById(userId)

        updateUserData(updateCurrentUserRequest, user)
        userAccountRepository.save(user)

        val isRegularUser = isRegularUser(user)
        if (isRegularUser) {
            val customer = customerService.getCustomerByUserId(user.id!!)
            customer.name = updateCurrentUserRequest.name!!
            customer.phone = updateCurrentUserRequest.phone!!
            customer.address = updateCurrentUserRequest.address!!
        }

        return createUserResponse(user)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateCurrentUserPassword(updateCurrentUserPasswordRequest: UpdateCurrentUserPasswordRequest): String {
        validationUtil.validate(updateCurrentUserPasswordRequest)
        val userId = utilities.getUserId()
        val user = findById(userId)

        if (!user.comparePassword(updateCurrentUserPasswordRequest.passwordConfirmation)) {
            throw ValidationCustomException("Password incorrect", "password")
        }

        user.updatePassword(passwordEncoder.encode(updateCurrentUserPasswordRequest.password))
        userAccountRepository.save(user)
        return "Password successfully updated";
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateCurrentUserEmail(updateCurrentUserEmailRequest: UpdateCurrentUserEmailRequest): String {
        validationUtil.validate(updateCurrentUserEmailRequest)
        val userId = utilities.getUserId()
        val user = findById(userId)

        if (!user.comparePassword(updateCurrentUserEmailRequest.passwordConfirmation)) {
            throw ValidationCustomException("Password incorrect", "password")
        }

        val random = Random()
        val confirmationEmailToken = String.format("%06d", random.nextInt(999999)) // token 6 digit (000000-999999)
        val expiryTime = LocalDateTime.now().plusMinutes(10) // 10 menit token

        user.pendingEmail = updateCurrentUserEmailRequest.newEmail
        user.confirmationEmailToken = confirmationEmailToken
        user.confirmationTokenExpiry = expiryTime
        userAccountRepository.save(user)

        val subject = "Email Change Confirmation"
        val text = """
            Hello,
            
            Please use the following 6-digit token to confirm your email change:
            Token: $confirmationEmailToken
            
            This token will expire at ${expiryTime.toString()}.
            If you did not request this change, please ignore this email.
            
            Thank you,
            Application Team
        """.trimIndent()
        sendEmail(user, subject, text)
        return "Confirmation token sent to ${updateCurrentUserEmailRequest.newEmail}. Please enter the token in the application."
    }
    @Transactional(rollbackFor = [Exception::class])
    override fun emailConfirmation(confirmEmailTokenRequest: ConfirmEmailTokenRequest): UserResponse<String> {
        val userId = utilities.getUserId()
        val user = userAccountRepository.findByConfirmationEmailToken(confirmEmailTokenRequest.confirmationEmailToken)
            ?: throw ValidationCustomException("Invalid token", "token")

        if (user.id != userId) {
            throw NotFoundException("Token not found")
        }

        if (user.confirmationTokenExpiry?.isBefore(LocalDateTime.now()) == true) {
            user.confirmationEmailToken = null
            user.confirmationTokenExpiry = null
            user.pendingEmail = null
            userAccountRepository.save(user)
            throw ValidationCustomException("Token has expired", "token")
        }

        //  Update email
        user.email = user.pendingEmail ?: throw ValidationCustomException("New email not found", "email")
        user.confirmed = true
        user.confirmationEmailToken = null
        user.confirmationTokenExpiry = null
        user.pendingEmail = null
        userAccountRepository.save(user)

        return createUserResponse(user);
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getUserByUsername(username: String): UserResponse<String> {
        val user = findByUsername(username)
        return createUserResponse(user)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun disabledOrEnabledUserById(id: String): String {
        val rawId = utilities.decodeId(id)
        val user = findById(rawId)

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
            .map { createUserResponse(it) }
        return users
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAdminAll(): List<UserResponse<String>> {
        val response = userAccountRepository.findAll();
        val users = response
            .filter { user ->
                user.roles.size == 2 && user.roles.any { role -> role.role == UserRoleEnum.ROLE_ADMIN }}
            .map { createUserResponse(it) }
        return users
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateAdminById(id: String, request: UpdateAdminRequest): UserResponse<String> {
        validationUtil.validate(request)
        val rawId = utilities.decodeId(id)
        val user = findById(rawId)

        if (user.roles.any { it.role == UserRoleEnum.ROLE_SUPER_ADMIN } || user.roles.size == 1 && user.roles.any { it.role == UserRoleEnum.ROLE_USER }) {
            throw ForbiddenException(StatusMessage.ACCESS_DENIED)
        }

        this.updateEmailIfChange(request.email!!, user)
        this.updateUsernameIfChange(request.username!!, user)

        if (!request.password.isNullOrBlank()) {
            user.updatePassword(passwordEncoder.encode(request.password))
        }

        userAccountRepository.save(user)
        return createUserResponse(user)
    }

    private fun findById(id: Int): UserAccount {
        return userAccountRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.USER_NOT_FOUND)
        }
    }

    private fun findByUsername(username: String): UserAccount {
        return userAccountRepository.findByUsername(username).orElseThrow {
            throw NotFoundException(StatusMessage.USER_NOT_FOUND)
        }
    }

    private fun updateUserData(updateCurrentUserRequest: UpdateCurrentUserRequest, user: UserAccount) {
        updateUsernameIfChange(updateCurrentUserRequest.username!!, user)
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

    private fun isRegularUser(user: UserAccount): Boolean {
        val roleNames = user.roles.mapNotNull { it.role }
        return roleNames.size == 1 && roleNames.contains(UserRoleEnum.ROLE_USER)
    }

    private fun createUserResponse(user: UserAccount): UserResponse<String> {
        val hashedId = utilities.encodeId(user.id!!)
        val isRegularUser = isRegularUser(user)

        // Hanya ambil customer jika role-nya hanya ROLE_USER
        val customer = if (isRegularUser) {
            customerService.getCustomerByUserId(user.id!!)
        } else null

        return UserResponse(
            id = hashedId,
            name = customer?.name,
            phone = customer?.phone,
            address = customer?.address,
            email = user.email,
            username = user.username,
            isEnable = user.isEnable,
            roles = user.roles.mapNotNull { it.role },
            createdAt = user.createdAt.toString(),
            updatedAt = user.updatedAt.toString(),
        )
    }

    private fun sendEmail(user: UserAccount, subject: String, text: String) {
        val message = SimpleMailMessage()
        message.setTo(user.pendingEmail)
        message.subject = subject
        message.text = text
        mailSender.send(message)
    }
}