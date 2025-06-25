package com.belajar.api.kotlin.controller

import com.belajar.api.kotlin.constant.ApiUrl
import com.belajar.api.kotlin.constant.StatusMessage

import com.belajar.api.kotlin.entities.WebResponse
import com.belajar.api.kotlin.entities.payment.MidtransWebhookRequest
import com.belajar.api.kotlin.service.PaymentService

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
@RequestMapping(ApiUrl.API_URL + ApiUrl.PAYMENT_URL)
@Tag(name = "Payment", description = "Payment API")
class PaymentController(
    private val paymentService: PaymentService,
    private val utilities: Utilities,
) {

    @PostMapping("/webhook", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun midtransWebhook(
        @RequestBody payload: MidtransWebhookRequest
    ): ResponseEntity<WebResponse<String>> =
        utilities.handleRequest({ paymentService.handleWebhook(payload) }, HttpStatus.OK, StatusMessage.SUCCESS_UPDATE)

    @Operation(summary = "Synchronize payment status")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    @GetMapping("/status/{orderId}")
    fun syncPaymentStatus(@PathVariable orderId: String): ResponseEntity<WebResponse<String>> =
        utilities.handleRequest({ paymentService.syncPaymentStatus(orderId) }, HttpStatus.OK, StatusMessage.SUCCESS_RETRIEVE)

}
