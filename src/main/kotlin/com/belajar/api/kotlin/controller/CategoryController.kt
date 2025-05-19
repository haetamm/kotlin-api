package com.belajar.api.kotlin.controller

import com.belajar.api.kotlin.constant.ApiUrl
import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.WebResponse
import com.belajar.api.kotlin.entities.category.CategoryRequest
import com.belajar.api.kotlin.entities.category.CategoryResponse
import com.belajar.api.kotlin.entities.category.UpdateCategoryRequest
import com.belajar.api.kotlin.service.CategoryService
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
@RequestMapping(ApiUrl.API_URL + ApiUrl.CATEGORY_URL)
@Tag(name = "Category", description = "Category API")
class CategoryController (private val categoryService: CategoryService, private val utilities: Utilities) {

    @Operation(summary = "Super admin, and Admin create new category")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun save(
        @RequestBody categoryRequest: CategoryRequest,
    ): ResponseEntity<WebResponse<CategoryResponse>> =
        utilities.handleRequest({ categoryService.save(categoryRequest) }, HttpStatus.CREATED, StatusMessage.SUCCESS_CREATE)

    @Operation(summary = "Super admin, Admin and User get all category")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll(): ResponseEntity<WebResponse<List<CategoryResponse>>> =
        utilities.handleRequest ({ categoryService.getAll() }, HttpStatus.OK, StatusMessage.SUCCESS_RETRIEVE_LIST)
    @Operation(summary = "Super admin and Admin update category by id")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PutMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@RequestBody request: UpdateCategoryRequest, @PathVariable id: String): ResponseEntity<WebResponse<CategoryResponse>> =
        utilities.handleRequest ({ categoryService.update(request, id)}, HttpStatus.OK, StatusMessage.SUCCESS_UPDATE)

    @Operation(summary = "Super admin and Admin delete category")
    @SecurityRequirement(name = "Authorization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @DeleteMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun delete(@PathVariable id: String): ResponseEntity<WebResponse<String>> =
        utilities.handleRequest ({ categoryService.delete(id) }, HttpStatus.OK, StatusMessage.SUCCESS)

}