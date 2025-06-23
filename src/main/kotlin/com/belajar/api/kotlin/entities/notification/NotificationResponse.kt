package com.belajar.api.kotlin.entities.notification

data class NotificationResponse(
    var id: String,
    var title: String,
    var message: String,
    var billId: String,
    var customerName: String,
    var isRead: Boolean,
    var createdAt: String,
)
