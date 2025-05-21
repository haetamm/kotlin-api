package com.belajar.api.kotlin.model

import com.belajar.api.kotlin.constant.TableName
import jakarta.persistence.*

@Entity
@Table(name = TableName.M_CART)
data class Cart(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL])
    var items: List<CartItem>? = null

)