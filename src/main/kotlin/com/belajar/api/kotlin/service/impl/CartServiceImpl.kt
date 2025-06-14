package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.cart.CartRequest
import com.belajar.api.kotlin.entities.cart.DeleteCartItemRequest
import com.belajar.api.kotlin.entities.cart_item.CartItemResponse
import com.belajar.api.kotlin.exception.BadRequestException
import com.belajar.api.kotlin.model.Cart
import com.belajar.api.kotlin.model.CartItem
import com.belajar.api.kotlin.repository.CartItemRepository
import com.belajar.api.kotlin.repository.CartRepository
import com.belajar.api.kotlin.service.CartService
import com.belajar.api.kotlin.service.CustomerService
import com.belajar.api.kotlin.service.MenuService
import com.belajar.api.kotlin.utils.Utilities
import com.belajar.api.kotlin.validation.ValidationUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartServiceImpl(
    private val validationUtil: ValidationUtil,
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val customerService: CustomerService,
    private val menuService: MenuService,
    private val utilities: Utilities
) : CartService {

    @Transactional(rollbackFor = [Exception::class])
    override fun save(request: CartRequest): List<CartItemResponse> {
        validationUtil.validate(request)
        val userId = utilities.getUserId()
        val customer = customerService.getCustomerByUserId(userId)

        var cart = cartRepository.findByCustomerId(customer.id.toString()).firstOrNull() ?:
        Cart(
            customer = customer,
            items = mutableListOf()
        )

        val cartItems = request.menuRequest.map { itemRequest ->
            val rawId = utilities.decodeUuid(itemRequest.menuId)
            val menu = menuService.findById(rawId)
            val existingItem = cart.items?.find { it.menu.id == rawId }

            if (existingItem != null) {
                val updatedQty = existingItem.qty + itemRequest.qty
                if (updatedQty <= 0) {
                    throw BadRequestException("Quantity must be greater than 0")
                }
                existingItem.qty = updatedQty
                existingItem
            } else {
                if (itemRequest.qty <= 0) {
                    throw BadRequestException("Quantity must be greater than 0")
                }
                CartItem(
                    cart = cart,
                    menu = menu,
                    qty = itemRequest.qty,
                )
            }
        }

        if (cart.id == null) {
            cart = cartRepository.save(cart)
            cartItems.forEach { if (it.id == null) it.cart = cart }
        }

        val savedCartItems = cartItemRepository.saveAll(cartItems)
        cart.items = savedCartItems
        cartRepository.save(cart)

        return savedCartItems.map { createCartItemResponse(it) }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAll(): List<CartItemResponse> {
        val userId = utilities.getUserId()
        val customer = customerService.getCustomerByUserId(userId)
        val cart = cartRepository.findByCustomerId(customer.id!!).firstOrNull()
        return cart?.items?.map { createCartItemResponse(it) } ?: emptyList()
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun deleteByMenuId(request: DeleteCartItemRequest): String {
        validationUtil.validate(request)
        val userId = utilities.getUserId()
        val customer = customerService.getCustomerByUserId(userId)
        val cart = findByCustomerId(customer.id.toString())

        val itemsToDelete = request.items.map { utilities.decodeUuid(it.menuId) }
        val itemsToRemove = cart.items?.filter { it.menu.id in itemsToDelete } ?: emptyList()

        if (itemsToRemove.isEmpty()) {
            throw BadRequestException("No matching items found in cart")
        }

        cart.items = cart.items?.filter { it.menu.id !in itemsToDelete }
        cartItemRepository.deleteAll(itemsToRemove)
        cartRepository.save(cart)

        return "Successfully deleted ${itemsToRemove.size} item's from cart"
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun findByCustomerId(id: String): Cart {
        return cartRepository.findByCustomerId(id)
            .firstOrNull() ?: throw BadRequestException(StatusMessage.CART_NOT_FOUND)
    }

    private fun createCartItemResponse(cartItem: CartItem): CartItemResponse {
        val hashedId = utilities.encodeUuid(cartItem.menu.id!!)
        return CartItemResponse(
            id = cartItem.id!!,
            menuId = hashedId,
            name = cartItem.menu.name,
            image = cartItem.menu.image?.id,
            qty = cartItem.qty,
            price = cartItem.menu.price
        )
    }
}