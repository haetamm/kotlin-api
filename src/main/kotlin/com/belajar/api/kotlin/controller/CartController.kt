package com.belajar.api.kotlin.controller

import com.belajar.api.kotlin.constant.ApiUrl
import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.WebResponse
import com.belajar.api.kotlin.entities.cart.CartRequest
import com.belajar.api.kotlin.entities.cart.DeleteCartItemRequest
import com.belajar.api.kotlin.entities.cart_item.CartItemResponse
import com.belajar.api.kotlin.service.CartService
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
@RequestMapping(ApiUrl.API_URL + ApiUrl.CART_URL)
@Tag(name = "Cart", description = "Cart API")
class CartController(
    private val cartService: CartService,
    private val utilities: Utilities,
) {

    @Operation(summary = "User create/update cart")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasRole('USER') && !hasRole('ADMIN') && !hasRole('SUPER_ADMIN')")
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun save(@RequestBody request: CartRequest): ResponseEntity<WebResponse<List<CartItemResponse>>> =
        utilities.handleRequest({ cartService.save(request) }, HttpStatus.CREATED, StatusMessage.SUCCESS_UPDATE)

    @Operation(summary = "User get cart")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasRole('USER') && !hasRole('ADMIN') && !hasRole('SUPER_ADMIN')")
    @GetMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getAll(): ResponseEntity<WebResponse<List<CartItemResponse>>> =
        utilities.handleRequest({ cartService.getAll() }, HttpStatus.OK, StatusMessage.SUCCESS_RETRIEVE)

    @Operation(summary = "User delete cart item")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasRole('USER') && !hasRole('ADMIN') && !hasRole('SUPER_ADMIN')")
    @DeleteMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun deleteByMenuId(@RequestBody request: DeleteCartItemRequest): ResponseEntity<WebResponse<String>> =
        utilities.handleRequest({ cartService.deleteByMenuId(request) }, HttpStatus.OK, StatusMessage.SUCCESS)

}