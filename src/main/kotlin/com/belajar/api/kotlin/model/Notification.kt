package com.belajar.api.kotlin.model

import com.belajar.api.kotlin.constant.TableName
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = TableName.M_NOTIFICATION)
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, length = 1000)
    var message: String,

    @Column(nullable = false)
    var billId: String,

    @Column(nullable = false)
    var customerName: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    var recipient: UserAccount,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
