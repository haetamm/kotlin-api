package com.belajar.api.kotlin.repository

import com.belajar.api.kotlin.model.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Int> {
    fun findAllByRecipientIdOrderByCreatedAtDesc(userId: Int): List<Notification>
}
