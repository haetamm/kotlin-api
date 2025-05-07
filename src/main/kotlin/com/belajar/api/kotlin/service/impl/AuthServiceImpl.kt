package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.UserRoleEnum
import com.belajar.api.kotlin.entities.customer.NewAccountRequest
import com.belajar.api.kotlin.entities.user.*
import com.belajar.api.kotlin.exception.ForbiddenException
import com.belajar.api.kotlin.exception.UnauthorizedException
import com.belajar.api.kotlin.model.UserAccount
import com.belajar.api.kotlin.model.UserRole
import com.belajar.api.kotlin.repository.UserAccountRepository
import com.belajar.api.kotlin.service.AuthService
import com.belajar.api.kotlin.service.CustomerService
import com.belajar.api.kotlin.service.JwtService
import com.belajar.api.kotlin.service.UserRoleService
import com.belajar.api.kotlin.validation.ValidationUtil
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AuthServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    private val userRoleService: UserRoleService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    val validationUtil: ValidationUtil,
    val mailSender: JavaMailSender,
    val customerService: CustomerService,
): AuthService {

    @Value("\${template_api.super-admin.email}")
    private lateinit var superAdminEmail: String

    @Value("\${template_api.super-admin.username}")
    private lateinit var superAdminUsername: String

    @Value("\${template_api.super-admin.password}")
    private lateinit var superAdminPassword: String

    @Value("\${template_api.url-frontend}")
    private lateinit var urlFrontend: String

    @Transactional(rollbackFor = [Exception::class])
    @PostConstruct
    fun initSuperAdmin() {
        val account = userAccountRepository.findByUsername(superAdminUsername)
        if (account.isPresent) return

        val superAdmin = userRoleService.saveOrGet(UserRoleEnum.ROLE_SUPER_ADMIN)
        val admin = userRoleService.saveOrGet(UserRoleEnum.ROLE_ADMIN)
        val user = userRoleService.saveOrGet(UserRoleEnum.ROLE_USER)

        userAccountRepository.saveAndFlush(UserAccount(
            email = superAdminEmail,
            username = superAdminUsername,
            password = passwordEncoder.encode(superAdminPassword),
            roles = listOf(superAdmin, admin, user),
            isEnable = true
        ))
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun registerUser(request: RegisterRequest): String {
        validationUtil.validate(request)

        val userRole = userRoleService.saveOrGet(UserRoleEnum.ROLE_USER)
        val user = saveToUserAccountRepository(request, listOf(userRole), false, confirmationToken = generateToken())

        customerService.saveAccount(
            NewAccountRequest(
                name = request.name!!,
                phone = request.phone!!,
                userAccount = user
            )
        )

        val subject = "Confirm your email and activated your account"
        val text = "Click the link to confirm your email: ${urlFrontend}/confirm?token=${user.confirmationToken}"
        sendEmail(user, subject, text)
        return "Check your email for confirmation link."
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun confirm(token: String): String {
        val user = userAccountRepository.findByConfirmationToken(token) ?: throw ForbiddenException("Invalid token")
        user.confirmed = true
        user.isEnable = true
        user.confirmationToken = null
        userAccountRepository.save(user)
        return "Email confirmed successfully."
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun forgotPassword(request: ForgotPasswordRequest): String {
        validationUtil.validate(request)
        val user = userAccountRepository.findByEmail(request.email!!) ?: throw UnauthorizedException("Email not found")
        val token = generateToken()
        user.resetPasswordToken = token
        userAccountRepository.save(user)
        val subject = "Reset Password"
        val text = "To reset your password, click the link below:\n ${urlFrontend}/reset-password?token=$token"
        sendEmail(user, subject, text)
        return "Password reset link sent to your email."
    }

    override fun validateToken(): UserAccount? {
        val userId = SecurityContextHolder.getContext().authentication.name
        println(userId)
        return userAccountRepository.findAndGetById(userId.toInt()).orElse(null)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun resetPassword(token: String, request: ResetPasswordRequest): String {
        validationUtil.validate(request)
        val user = userAccountRepository.findByResetPasswordToken(token) ?: throw ForbiddenException("Invalid token")
        val hashPassword = passwordEncoder.encode(request.password)
        user.updatePassword(hashPassword)
        user.resetPasswordToken = null
        userAccountRepository.save(user)
        return "Password reset successfully"
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun registerAdmin(request: RegisterRequest): RegisterResponse {
        validationUtil.validate(request)
        val userRole = userRoleService.saveOrGet(UserRoleEnum.ROLE_USER)
        val adminRole = userRoleService.saveOrGet(UserRoleEnum.ROLE_ADMIN)
        val userAccount = saveToUserAccountRepository(request, listOf(userRole, adminRole), true, null)

        val roles: List<String> = userAccount.roles.map { role ->
            role.role?.name ?: "Unknown"
        }

        return RegisterResponse(
            username = userAccount.username,
            roles = roles
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun login(request: LoginRequest): LoginResponse {
        validationUtil.validate(request)

        val authentication = UsernamePasswordAuthenticationToken(request.username, request.password)
        val authenticate = authenticationManager.authenticate(authentication)
        SecurityContextHolder.getContext().authentication = authenticate
        val userAccount = authenticate.principal as UserAccount

        val token = jwtService.generateToken(userAccount)
        val roles = userAccount.authorities.map { it.authority }
        return LoginResponse(
            username = userAccount.username,
            token = token,
            roles = roles
        )
    }

    private fun saveToUserAccountRepository(request: RegisterRequest, userRoles: List<UserRole>, isEnable: Boolean, confirmationToken: String?): UserAccount {
        val hashedPassword = passwordEncoder.encode(request.password)
        val userAccount: UserAccount = userAccountRepository.saveAndFlush(
            UserAccount(
                email = request.email!!,
                username = request.username!!,
                password = hashedPassword,
                roles = userRoles,
                isEnable = isEnable,
                confirmationToken = confirmationToken
            )
        )
        return userAccount
    }

    private fun generateToken(): String {
        return UUID.randomUUID().toString()
    }

    private fun sendEmail(user: UserAccount, subject: String, text: String) {
        val message = SimpleMailMessage()
        message.setTo(user.email)
        message.subject = subject
        message.text = text
        mailSender.send(message)
    }

}