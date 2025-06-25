package com.belajar.api.kotlin.entities.notification

data class EmailNotificationMessage(
    val recipientEmail: String,
    val subject: String,
    val message: String,
    val customerName: String,
    val billId: String,
    val template: String = "templates/email_order_notification.html"
)

