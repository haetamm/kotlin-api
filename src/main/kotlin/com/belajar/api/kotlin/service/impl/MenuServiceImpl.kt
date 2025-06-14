package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.ApiUrl
import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.image.ImageResponse
import com.belajar.api.kotlin.entities.menu.MenuRequest
import com.belajar.api.kotlin.entities.menu.MenuResponse
import com.belajar.api.kotlin.entities.menu.SearchMenuRequest
import com.belajar.api.kotlin.entities.menu.UpdateMenuRequest
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.exception.ValidationCustomException
import com.belajar.api.kotlin.model.Menu
import com.belajar.api.kotlin.repository.CartItemRepository
import com.belajar.api.kotlin.repository.MenuRepository
import com.belajar.api.kotlin.service.CategoryService
import com.belajar.api.kotlin.service.ImageService
import com.belajar.api.kotlin.service.MenuService
import com.belajar.api.kotlin.specification.MenuSpecification
import com.belajar.api.kotlin.utils.Utilities
import com.belajar.api.kotlin.validation.ValidationUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class MenuServiceImpl(
    private val imageService: ImageService,
    private val menuRepository: MenuRepository,
    private val specification: MenuSpecification,
    private val validationUtil: ValidationUtil,
    private val categoryService: CategoryService,
    private val cartItemRepository: CartItemRepository,
    private val utilities: Utilities

): MenuService {

    @Transactional(rollbackFor = [Exception::class])
    override fun save(request: MenuRequest, image: MultipartFile): MenuResponse {
        validationUtil.validate(request)
        val imageResult = imageService.save(image)
        val category = categoryService.getById(request.categoryId)

        val menu = menuRepository.saveAndFlush(
            Menu(
                name = request.name,
                price = request.price,
                image = imageResult,
                category = category,
            )
        )
        return createMenuResponse(menu)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun saveBulk(requests: List<MenuRequest>): List<MenuResponse> {
        validationUtil.validateAll(requests)

        val responses = mutableListOf<MenuResponse>()
        requests.forEach { request ->
            val category = categoryService.getById(request.categoryId)
            val menu = Menu(
                name = request.name,
                price = request.price,
                category = category
            )
            menuRepository.saveAndFlush(menu)
            val response = createMenuResponse(menu)
            responses.add(response)
        }
        return responses
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getById(id: String): MenuResponse {
        val rawId = utilities.decodeUuid(id)
        val menu = findById(rawId)
        return createMenuResponse(menu)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateById(request: UpdateMenuRequest, updateImage: MultipartFile?, id: String): MenuResponse {
        validationUtil.validate(request)
        val category = categoryService.getById(request.categoryId)

        val rawId = utilities.decodeUuid(id)
        val menu = findById(rawId)
        val image = menu.image

        if (updateImage !== null) {
            if (image === null) {
                val newImage = imageService.save(updateImage)
                menu.image = newImage
            } else {
                val changeImage = imageService.updateById(menu.image?.id!!, updateImage)
                menu.image = changeImage
            }
        }

        menu.name = request.name
        menu.price = request.price
        menu.category = category

        return  createMenuResponse(menu)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAll(request: SearchMenuRequest): Page<MenuResponse> {
        val menuSpecification = specification.specification(request)

        val sort = Sort.by(Sort.Direction.fromString(request.direction), request.sortBy)
        val page = if (request.page <= 0) 1 else request.page
        val pageable = PageRequest.of(page - 1, request.size, sort)

        val menus = menuRepository.findAll(menuSpecification, pageable)
        return menus.map { menu ->
            createMenuResponse(menu)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun delete(id: String): String {
        val rawId = utilities.decodeUuid(id)
        val menu = findById(rawId)
        menuRepository.softDelete(menu.id!!)
        val imageId = menu.image?.id
        if (!imageId.isNullOrBlank()) {
            imageService.softDeleteById(imageId)
        }

        cartItemRepository.deleteByMenuId(menu.id)
        return StatusMessage.SUCCESS_DELETE
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getMenuById(id: String): Menu {
        return findById(id)
    }

    private fun createMenuResponse(menu: Menu): MenuResponse {
        val imageResponse = menu.image?.let {
            ImageResponse(
                id = it.id!!,
                name = it.name,
                url = "${ApiUrl.API_URL}${ApiUrl.MENU_URL}/${it.id}/images"
            )
        }
        val hashedId = utilities.encodeUuid(menu.id!!)
        return MenuResponse(
            id = hashedId,
            name = menu.name,
            price = menu.price,
            image = imageResponse,
            category = menu.category.name,
            categoryId = menu.category.id
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun findById(id: String): Menu {
        val menu = menuRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.MENU_NOT_FOUND)
        }
        if (menu.deleted) {
            throw NotFoundException(StatusMessage.MENU_NOT_FOUND)
        }
        return menu
    }
}