package com.belajar.api.kotlin.controller

import com.belajar.api.kotlin.constant.ApiUrl
import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.WebResponse
import com.belajar.api.kotlin.entities.notification.NotificationResponse
import com.belajar.api.kotlin.entities.user.*
import com.belajar.api.kotlin.service.NotificationService
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
@RequestMapping(ApiUrl.API_URL + ApiUrl.NOTIFICATION_URL)
@Tag(name = "Notification", description = "Notification API")
class NotificationController(
    private val notificationService: NotificationService,
    private val utilities: Utilities
) {

    @Operation(summary = "Get Notification By User Current")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByUserCurrent (): ResponseEntity<WebResponse<List<NotificationResponse>>> =
        utilities.handleRequest ({ notificationService.getNotificationByUserCurrent() }, HttpStatus.OK, StatusMessage.SUCCESS_RETRIEVE)

    @Operation(summary = "Update Notification By Id")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PutMapping(
        path = ["/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateById (@PathVariable("id") id: String): ResponseEntity<WebResponse<String>> =
        utilities.handleRequest ({ notificationService.updateById(id) }, HttpStatus.OK, StatusMessage.SUCCESS_UPDATE)

}