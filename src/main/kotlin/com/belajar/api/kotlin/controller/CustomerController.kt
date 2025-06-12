package com.belajar.api.kotlin.controller

import com.belajar.api.kotlin.constant.ApiUrl
import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.PaginationResponse
import com.belajar.api.kotlin.entities.WebResponse
import com.belajar.api.kotlin.entities.customer.CustomerRequest
import com.belajar.api.kotlin.entities.customer.CustomerResponse
import com.belajar.api.kotlin.entities.customer.SearchCustomerRequest
import com.belajar.api.kotlin.entities.customer.UpdateCustomerRequest
import com.belajar.api.kotlin.service.CustomerService
import com.belajar.api.kotlin.utils.Utilities
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiUrl.API_URL + ApiUrl.CUSTOMER_URL)
@Tag(name = "Customer", description = "Customer API")
class CustomerController(
    private val customerService: CustomerService,
    private val utilities: Utilities
) {

    @Operation(summary = "Super admin and Admin create new customer")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun save(@RequestBody request: CustomerRequest): ResponseEntity<WebResponse<CustomerResponse>> =
        utilities.handleRequest ({ customerService.saveOrGet(request) }, HttpStatus.CREATED, StatusMessage.SUCCESS_CREATE)

    @Operation(summary = "Super admin and Admin create new list of customer")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping(path = ["/bulk"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun saveBulk(@RequestBody request: List<CustomerRequest>): ResponseEntity<WebResponse<List<CustomerResponse>>> =
        utilities.handleRequest ({ customerService.saveBulk(request) }, HttpStatus.CREATED, StatusMessage.SUCCESS_CREATE_LIST)

    @Operation(summary = "Super admin and Admin get customer by id")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: String): ResponseEntity<WebResponse<CustomerResponse>> =
        utilities.handleRequest ({ customerService.getById(id) }, HttpStatus.OK, StatusMessage.SUCCESS_RETRIEVE)

    @Operation(summary = "Super admin and Admin get all customer")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll(@ParameterObject @ModelAttribute request: SearchCustomerRequest): ResponseEntity<WebResponse<List<CustomerResponse>>> {
        val customerResponse = customerService.getAll(request)
        val paginationResponse = PaginationResponse(
            totalPages = customerResponse.totalPages,
            totalElement = customerResponse.totalElements,
            page = customerResponse.number + 1,
            size = customerResponse.size,
            hasNext = customerResponse.hasNext(),
            hasPrevious = customerResponse.hasPrevious()
        )
        val response = WebResponse(
            code = HttpStatus.OK.value(),
            status = StatusMessage.SUCCESS_RETRIEVE_LIST,
            data = customerResponse.content,
            paginationResponse = paginationResponse
        )
        return ResponseEntity.status(HttpStatus.OK).body(response)

    }

    @Operation(summary = "Super admin and Admin update customer")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PutMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@RequestBody request: UpdateCustomerRequest, @PathVariable id: String): ResponseEntity<WebResponse<CustomerResponse>> =
        utilities.handleRequest ({ customerService.update(request, id) }, HttpStatus.OK, StatusMessage.SUCCESS_UPDATE)

    @Operation(summary = "Super admin and Admin delete customer")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @DeleteMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun delete(@PathVariable id: String): ResponseEntity<WebResponse<String>> =
        utilities.handleRequest ({ customerService.delete(id) }, HttpStatus.OK, StatusMessage.SUCCESS)
    
}