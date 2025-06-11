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
        val userId = getUserId()
        val customer = customerService.getCustomerByUserId(userId.toInt())

        // Try to find existing cart, or create a new one if none exists
        var cart = cartRepository.findByCustomerId(customer.id.toString()).firstOrNull() ?:
            Cart(
                customer = customer,
                items = mutableListOf()
            )

        // Process cart items
        val cartItems = request.menuRequest.map { itemRequest ->
            val menu = menuService.findById(itemRequest.menuId)
            val existingItem = cart.items?.find { it.menu.id == itemRequest.menuId }

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

        // Save the cart first if it's new (to generate ID)
        if (cart.id == null) {
            cart = cartRepository.save(cart)
            // Update cart reference in new cart items
            cartItems.forEach { if (it.id == null) it.cart = cart }
        }

        // Save all CartItem
        val savedCartItems = cartItemRepository.saveAll(cartItems)

        // Update items in Cart and save
        cart.items = savedCartItems
        cartRepository.save(cart)

        // Convert CartItem to CartItemResponse
        return savedCartItems.map { item ->
            CartItemResponse(
                id = item.id!!,
                menuId = item.menu.id!!,
                name = item.menu.name,
                image = item.menu.image?.id,
                qty = item.qty,
                price = item.menu.price
            )
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAll(): List<CartItemResponse> {
        val userId = getUserId()
        val customer = customerService.getCustomerByUserId(userId.toInt())
        val cart = cartRepository.findByCustomerId(customer.id!!).firstOrNull()
        return cart?.items?.map { item ->
            CartItemResponse(
                id = item.id!!,
                menuId = item.menu.id!!,
                name = item.menu.name,
                image = item.menu.image?.id,
                qty = item.qty,
                price = item.menu.price
            )
        } ?: emptyList()
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun deleteByMenuId(request: DeleteCartItemRequest): String {
        validationUtil.validate(request)
        val userId = getUserId()
        val customer = customerService.getCustomerByUserId(userId.toInt())
        val cart = findByCustomerId(customer.id.toString())

        val itemsToDelete = request.items.map { it.menuId }
        val itemsToRemove = cart.items?.filter { it.menu.id in itemsToDelete } ?: emptyList()

        if (itemsToRemove.isEmpty()) {
            throw BadRequestException("No matching items found in cart")
        }

        // Remove items from cart
        cart.items = cart.items?.filter { it.menu.id !in itemsToDelete }

        // Delete items from repository
        cartItemRepository.deleteAll(itemsToRemove)

        // Save updated cart
        cartRepository.save(cart)

        return "Successfully deleted ${itemsToRemove.size} item's from cart"
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun findByCustomerId(id: String): Cart {
        return cartRepository.findByCustomerId(id)
            .firstOrNull() ?: throw BadRequestException(StatusMessage.CART_NOT_FOUND)
    }

    private fun getUserId(): String {
        return SecurityContextHolder.getContext().authentication.name
    }

}