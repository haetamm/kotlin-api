package com.belajar.api.kotlin.model

import com.belajar.api.kotlin.constant.TableName
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = TableName.M_BILL)
data class Bill(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(name = "recipient_name")
    var recipientName: String? = null,

    @Column(name = "phone_number")
    var phone: String? = null,

    @Column(name = "delivery_address")
    var deliveryAddress : String? = null,

    @Column(name = "trans_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var transDate: Date,

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer,

    @ManyToOne
    @JoinColumn(name = "table_id")
    var table: TableRest? = null,

    @ManyToOne
    @JoinColumn(name = "trans_type", nullable = false)
    var transType: TransType,

    @OneToMany(mappedBy = "bill", cascade = [CascadeType.PERSIST])
    @JsonManagedReference
    var billDetails: List<BillDetail>? = null,

    @OneToOne
    @JoinColumn(name = "payment_id", unique = true)
    var payment: Payment? = null,
)
