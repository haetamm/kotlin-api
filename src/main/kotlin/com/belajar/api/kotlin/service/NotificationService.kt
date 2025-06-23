package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.entities.notification.NotificationResponse

interface NotificationService {
    fun getNotificationByUserCurrent(): List<NotificationResponse>
    fun updateById(id: String): String
}