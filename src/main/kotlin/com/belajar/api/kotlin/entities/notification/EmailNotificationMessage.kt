package com.belajar.api.kotlin.entities.notification

data class EmailNotificationMessage(
    val subject: String,
    val message: String,
    val recipientEmail: String,
    val billId: String,
    val customerName: String
)
