package com.belajar.api.kotlin.model

import com.belajar.api.kotlin.constant.TableName
import jakarta.persistence.*

@Entity
@Table(name = TableName.M_CART_ITEM)
data class CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: Cart,

    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    var menu: Menu,

    @Column(name = "qty", nullable = false)
    var qty: Int,

)