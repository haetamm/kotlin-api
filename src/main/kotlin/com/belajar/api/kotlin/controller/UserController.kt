package com.belajar.api.kotlin.controller

import com.belajar.api.kotlin.constant.ApiUrl
import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.WebResponse
import com.belajar.api.kotlin.entities.user.*
import com.belajar.api.kotlin.service.UserService
import com.belajar.api.kotlin.utils.Utilities
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(ApiUrl.API_URL + ApiUrl.USER_URL)
@Tag(name = "User", description = "User API")
class UserController(
    private val userService: UserService,
    private val utilities: Utilities
) {

    @Operation(summary = "Get User Current")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER') or authenticated")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserCurrent (): ResponseEntity<WebResponse<UserResponse<String>>> =
        utilities.handleRequest ({ userService.getCurrentUser() }, HttpStatus.OK, StatusMessage.SUCCESS_RETRIEVE)

    @Operation(summary = "Update User Current")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasRole('USER') && !hasRole('ADMIN') && !hasRole('SUPER_ADMIN')")
    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateCurrentUser (@RequestBody request: UpdateCurrentUserRequest): ResponseEntity<WebResponse<UserResponse<String>>> =
        utilities.handleRequest ({ userService.updateCurrentUser(request) }, HttpStatus.OK, StatusMessage.SUCCESS_UPDATE)

    @Operation(summary = "Update  Current User Password")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasRole('USER') && !hasRole('ADMIN') && !hasRole('SUPER_ADMIN')")
    @PutMapping(
        path = ["/credential/1"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateCurrentUserPassword (@RequestBody request: UpdateCurrentUserPasswordRequest): ResponseEntity<WebResponse<String>> =
        utilities.handleRequest ({ userService.updateCurrentUserPassword(request) }, HttpStatus.OK, StatusMessage.SUCCESS_UPDATE)

    @Operation(summary = "Current User Confirm email token")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasRole('USER') && !hasRole('ADMIN') && !hasRole('SUPER_ADMIN')")
    @PostMapping(
        path = ["/confirm-email"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun emailConfirmation (@RequestBody confirmEmailTokenRequest: ConfirmEmailTokenRequest): ResponseEntity<WebResponse<UserResponse<String>>> =
        utilities.handleRequest ({ userService.emailConfirmation(confirmEmailTokenRequest) }, HttpStatus.OK, StatusMessage.SUCCESS)

    @Operation(summary = "Update Current User Email")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasRole('USER') && !hasRole('ADMIN') && !hasRole('SUPER_ADMIN')")
    @PutMapping(
        path = ["/credential/2"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateCurrentUserEmail (@RequestBody updateCurrentUserEmailRequest: UpdateCurrentUserEmailRequest): ResponseEntity<WebResponse<String>> =
        utilities.handleRequest ({ userService.updateCurrentUserEmail(updateCurrentUserEmailRequest) }, HttpStatus.OK, StatusMessage.SUCCESS_UPDATE)

    @Operation(summary = "Super admin and Admin get User by username")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping(path = ["/{username}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserById (@PathVariable("username") username: String): ResponseEntity<WebResponse<UserResponse<String>>> =
        utilities.handleRequest ({ userService.getUserByUsername(username) }, HttpStatus.OK, StatusMessage.SUCCESS_RETRIEVE)

    @Operation(summary = "Super admin disable or enable Admin and User by id")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PutMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun disabledOrEnabledUserById (@PathVariable("id") id: String): ResponseEntity<WebResponse<String>> =
        utilities.handleRequest ({ userService.disabledOrEnabledUserById(id) }, HttpStatus.OK, StatusMessage.SUCCESS)

    @Operation(summary = "Super admin and Admin get all User")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping(path = ["/all"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserAll (): ResponseEntity<WebResponse<List<UserResponse<String>>>> =
        utilities.handleRequest ({ userService.getUserAll() }, HttpStatus.OK, StatusMessage.SUCCESS_RETRIEVE_LIST)

    @Operation(summary = "Super admin get all Admin")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @GetMapping(path = ["/admin"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAdminAll (): ResponseEntity<WebResponse<List<UserResponse<String>>>> =
        utilities.handleRequest ({ userService.getAdminAll() }, HttpStatus.OK, StatusMessage.SUCCESS_RETRIEVE_LIST)

    @Operation(summary = "Super admin update Admin by id")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PutMapping(path = ["/admin/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateAdminById (@PathVariable("id") id: Int, @RequestBody request: RegisterRequest): ResponseEntity<WebResponse<UserResponse<String>>> =
        utilities.handleRequest ({ userService.updateAdminById(id, request) }, HttpStatus.OK, StatusMessage.SUCCESS_UPDATE)

}