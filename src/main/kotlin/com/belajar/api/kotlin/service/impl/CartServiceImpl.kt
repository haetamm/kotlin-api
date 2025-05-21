package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.entities.cart.CartRequest
import com.belajar.api.kotlin.entities.cart_item.CartItemResponse
import com.belajar.api.kotlin.model.Cart
import com.belajar.api.kotlin.model.CartItem
import com.belajar.api.kotlin.repository.CartItemRepository
import com.belajar.api.kotlin.repository.CartRepository
import com.belajar.api.kotlin.service.CartService
import com.belajar.api.kotlin.service.CustomerService
import com.belajar.api.kotlin.service.MenuService
import com.belajar.api.kotlin.validation.ValidationUtil
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartServiceImpl(
    private val validationUtil: ValidationUtil,
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val customerService: CustomerService,
    private val menuService: MenuService
) : CartService {

    @Transactional(rollbackFor = [Exception::class])
    override fun save(request: CartRequest): List<CartItemResponse> {
        validationUtil.validate(request)

        val userId = SecurityContextHolder.getContext().authentication.name
        val customer = customerService.getCustomerByUserId(userId.toInt())

        val cart = cartRepository.findByCustomerId(customer.id!!)
            .firstOrNull() ?: cartRepository.save(Cart(customer = customer))

        val cartItems = request.menuRequest.map { itemRequest ->
            val menu = menuService.findById(itemRequest.menuId)
            val existingItem = cart.items?.find { it.menu.id == itemRequest.menuId }
            if (existingItem != null) {
                // Update qty jika menu sudah ada
                existingItem.qty += itemRequest.qty
                existingItem
            } else {
                // Buat CartItem baru
                CartItem(
                    cart = cart,
                    menu = menu,
                    qty = itemRequest.qty,
                    price = menu.price
                )
            }
        }

        // Simpan semua CartItem
        val savedCartItems = cartItemRepository.saveAll(cartItems)

        // Update items di Cart dan simpan
        cart.items = savedCartItems
        cartRepository.save(cart)

        // Konversi CartItem ke CartItemResponse
        return savedCartItems.map { item ->
            CartItemResponse(
                id = item.id!!,
                menuId = item.menu.id!!,
                name = item.menu.name,
                image = item.menu.image?.id,
                qty = item.qty,
                price = item.price
            )
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAll(): List<CartItemResponse> {
        val userId = SecurityContextHolder.getContext().authentication.name
        val customer = customerService.getCustomerByUserId(userId.toInt())
        val cart = cartRepository.findByCustomerId(customer.id!!).firstOrNull()
        return cart?.items?.map { item ->
            CartItemResponse(
                id = item.id!!,
                menuId = item.menu.id!!,
                name = item.menu.name,
                image = item.menu.image?.id,
                qty = item.qty,
                price = item.price
            )
        } ?: emptyList()
    }
}