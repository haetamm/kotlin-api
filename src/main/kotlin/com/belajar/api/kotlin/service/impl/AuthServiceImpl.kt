package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.UserRoleEnum
import com.belajar.api.kotlin.entities.customer.NewAccountRequest
import com.belajar.api.kotlin.entities.user.*
import com.belajar.api.kotlin.exception.ForbiddenException
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.model.UserAccount
import com.belajar.api.kotlin.model.UserRole
import com.belajar.api.kotlin.repository.UserAccountRepository
import com.belajar.api.kotlin.service.AuthService
import com.belajar.api.kotlin.service.CustomerService
import com.belajar.api.kotlin.service.JwtService
import com.belajar.api.kotlin.service.UserRoleService
import com.belajar.api.kotlin.utils.Utilities
import com.belajar.api.kotlin.validation.ValidationUtil
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import org.springframework.mail.javamail.MimeMessageHelper
import java.nio.file.Files
import java.nio.file.Paths
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap


@Service
class AuthServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    private val userRoleService: UserRoleService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val validationUtil: ValidationUtil,
    private val mailSender: JavaMailSender,
    private val customerService: CustomerService,
    private val utilities: Utilities,
    private val restTemplate: RestTemplate,
): AuthService {

    @Value("\${template_api.super-admin.email}")
    private lateinit var superAdminEmail: String

    @Value("\${template_api.super-admin.username}")
    private lateinit var superAdminUsername: String

    @Value("\${template_api.super-admin.password}")
    private lateinit var superAdminPassword: String

    @Value("\${template_api.url-frontend}")
    private lateinit var urlFrontend: String

    @Value("\${spring.mail.username}")
    private lateinit var emailFrom: String

    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
    private lateinit var googleClientId: String

    @Value("\${spring.security.oauth2.client.registration.google.client-secret}")
    private lateinit var googleClientSecret: String

    @Value("\${spring.security.oauth2.client.redirect.url}")
    private lateinit var googleRedirectUri: String

    @Value("\${spring.security.oauth2.grant.type}")
    private lateinit var grantType: String

    @Value("\${spring.security.oauth2.client.provider.google.token-uri}")
    private lateinit var googleTokenUrl: String

    @Value("\${spring.security.oauth2.client.provider.google.user-info-uri}")
    private lateinit var googleUserInfoUrl: String

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
    override fun socialite(code: String, scope: String): Any {
        val requestEntity: HttpEntity<MultiValueMap<String, String>>
        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

            val params = getTokenRequestParams(code, scope)
            requestEntity = HttpEntity(params, headers)

            val tokenResponse = restTemplate.postForObject(
                googleTokenUrl,
                requestEntity,
                GoogleTokenResponse::class.java
            ) ?: throw RuntimeException("Google token response is null")

            val userInfo = getGoogleUserProfile(tokenResponse.access_token)

            val existingUser = userAccountRepository.findByEmail(userInfo.email)
            return if (existingUser != null) {
                createLoginResponse(existingUser)
            } else {
                userInfo.username = userInfo.email.substringBefore("@")
                userInfo.tokenAccess = tokenResponse.access_token
                userInfo
            }

        } catch (e: HttpClientErrorException) {
            println("Google login error: ${e.statusCode} - ${e.responseBodyAsString}")
            throw RuntimeException("Error during Google login", e)
        } catch (e: Exception) {
            throw RuntimeException("Unexpected error during Google login", e)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun regUserWithGoogle(request: RegisterWithGoogleRequest): LoginResponse {
        validationUtil.validate(request)
        getGoogleUserProfile(request.tokenAccess!!)

        val rawPassword = request.password ?: generateRandomPassword()

        val userRole = userRoleService.saveOrGet(UserRoleEnum.ROLE_USER)

        val user = saveToUserAccountRepository(
            request.email!!,
            request.username!!,
            rawPassword,
            listOf(userRole),
            true,
            null
        )

        customerService.saveAccount(
            NewAccountRequest(
                name = request.name,
                phone = request.phone,
                userAccount = user
            )
        )

        return createLoginResponse(user)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun registerUser(request: RegisterRequest): String {
        validationUtil.validate(request)

        val userRole = userRoleService.saveOrGet(UserRoleEnum.ROLE_USER)
        val user = saveToUserAccountRepository(
            request.email!!,
            request.username!!,
            request.password!!,
            listOf(userRole),
            false,
            confirmationToken = generateToken()
        )

        customerService.saveAccount(
            NewAccountRequest(
                name = request.name!!,
                phone = request.phone!!,
                userAccount = user
            )
        )

        val subject = "Activate Your Account and Start Ordering Delicious Meals!"
        val confirmationUrl = "$urlFrontend/confirm?token=${user.confirmationToken}"
        val logoUrl = "$urlFrontend/img/logo.png"

        // Baca template HTML dari file
        val templatePath = Paths.get("src/main/resources/templates/email_confirm.html")
        var htmlContent = Files.readString(templatePath)

        // Ganti placeholder dengan data dinamis
        htmlContent = htmlContent.replace("\${user.username}", user.username ?: "Foodie")
        htmlContent = htmlContent.replace("\${confirmationUrl}", confirmationUrl)
        htmlContent = htmlContent.replace("\${logoUrl}", logoUrl)

        // Kirim email
        sendEmail(user, subject, htmlContent)

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
        val user = userAccountRepository.findByEmail(request.email!!) ?: throw NotFoundException("Email not found")
        val token = generateToken()
        user.resetPasswordToken = token
        userAccountRepository.save(user)

        val subject = "Reset Password"
        val resetPasswordUrl = "$urlFrontend/reset-password?token=$token"
        val logoUrl = "$urlFrontend/img/logo.png"

        // Baca template HTML dari file
        val templatePath = Paths.get("src/main/resources/templates/reset_password.html")
        var htmlContent = Files.readString(templatePath)

        // Ganti placeholder dengan data dinamis
        htmlContent = htmlContent.replace("\${user.username}", user.username ?: "Foodie")
        htmlContent = htmlContent.replace("\${resetPasswordUrl}", resetPasswordUrl)
        htmlContent = htmlContent.replace("\${logoUrl}", logoUrl)

        // Kirim email
        sendEmail(user, subject, htmlContent)

        return "Check your email for confirmation link."
    }

    override fun validateToken(): UserAccount? {
        val userId = SecurityContextHolder.getContext().authentication.name
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
    override fun registerAdmin(request: RegisterAdminRequest): AdminResponse {
        validationUtil.validate(request)
        val userRole = userRoleService.saveOrGet(UserRoleEnum.ROLE_USER)
        val adminRole = userRoleService.saveOrGet(UserRoleEnum.ROLE_ADMIN)
        val hashedPassword = passwordEncoder.encode(request.password)
        val userAccount: UserAccount = userAccountRepository.saveAndFlush(
            UserAccount(
                email = request.email!!,
                username = request.username!!,
                password = hashedPassword,
                roles = listOf(userRole, adminRole),
                isEnable = true,
            )
        )
        return createAdminResponse(userAccount)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun login(request: LoginRequest): LoginResponse {
        validationUtil.validate(request)

        val authentication = UsernamePasswordAuthenticationToken(request.username, request.password)
        val authenticate = authenticationManager.authenticate(authentication)
        SecurityContextHolder.getContext().authentication = authenticate
        val userAccount = authenticate.principal as UserAccount

        return createLoginResponse(userAccount)
    }

    private fun saveToUserAccountRepository(email: String, username: String, rawPassword: String, userRoles: List<UserRole>, isEnable: Boolean, confirmationToken: String?): UserAccount {
        val hashedPassword = passwordEncoder.encode(rawPassword)
        val userAccount: UserAccount = userAccountRepository.saveAndFlush(
            UserAccount(
                email = email,
                username = username,
                password = hashedPassword,
                roles = userRoles,
                isEnable = isEnable,
                confirmationToken = confirmationToken
            )
        )
        return userAccount
    }

    private fun createAdminResponse(user: UserAccount): AdminResponse {
        val roles: List<String> = user.roles.map { role ->
            role.role?.name ?: "Unknown"
        }

        val hashedId = utilities.encodeId(user.id!!)
        return AdminResponse(
            id = hashedId,
            username = user.username,
            email = user.email,
            isEnable = user.isEnable,
            roles = roles,
            createdAt = user.createdAt.toString(),
            updatedAt = user.updatedAt.toString()
        )
    }

    private fun generateToken(): String {
        return UUID.randomUUID().toString()
    }

    private fun sendEmail(user: UserAccount, subject: String, htmlContent: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setTo(user.email)
        helper.setSubject(subject)
        helper.setText(htmlContent, true) // true = isHtml
        helper.setFrom(emailFrom, "Warmakth Team")// Ganti dengan email pengirim Anda

        mailSender.send(message)
    }

    private fun getTokenRequestParams(code: String, scope: String): MultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()
        params.add("code", code)
        params.add("client_id", googleClientId.trim())
        params.add("client_secret", googleClientSecret.trim())
        params.add("redirect_uri", googleRedirectUri.trim())
        params.add("scope", scope)
        params.add("grant_type", grantType.trim())
        return params
    }

    private fun getGoogleUserProfile(accessToken: String): GoogleUserResponse {
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)
        val entity = HttpEntity<String>(headers)

        val response = restTemplate.exchange(
            googleUserInfoUrl,
            HttpMethod.GET,
            entity,
            GoogleUserResponse::class.java
        )

        return response.body ?: throw RuntimeException("Failed to retrieve Google user profile")
    }

    private fun createLoginResponse(user: UserAccount): LoginResponse {
        val token = jwtService.generateToken(user)
        val roles = user.authorities.map { it.authority }
        return LoginResponse(
            username = user.username,
            token = token,
            roles = roles
        )
    }

    private fun generateRandomPassword(length: Int = 8): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }

}