package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.notification.NotificationResponse
import com.belajar.api.kotlin.exception.ForbiddenException
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.model.Notification
import com.belajar.api.kotlin.repository.NotificationRepository
import com.belajar.api.kotlin.service.NotificationService
import com.belajar.api.kotlin.utils.Utilities
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationServiceImpl(
    private val utilities: Utilities,
    private val notificationRepository: NotificationRepository
) : NotificationService {

    @Transactional(readOnly = true)
    override fun getNotificationByUserCurrent(): List<NotificationResponse> {
        val userId = utilities.getUserId()

        val notifications = notificationRepository
            .findAllByRecipientIdOrderByCreatedAtDesc(userId)


        return notifications.map {
            val hashedId = utilities.encodeId(it.id!!)
            val hashedBillId = utilities.encodeUuid(it.billId)
            NotificationResponse(
                id = hashedId,
                title = it.title,
                message = it.message,
                billId = hashedBillId,
                customerName = it.customerName,
                isRead = it.isRead,
                createdAt = it.createdAt.toString()
            )
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateById(id: String): String {
        val userId = utilities.getUserId()
        val encodeId = utilities.decodeId(id)
        val notification = findById(encodeId)

        if (notification.recipient.id != userId) {
            throw ForbiddenException(StatusMessage.ACCESS_DENIED)
        }
        notification.isRead = true
        notificationRepository.save(notification)
        return StatusMessage.SUCCESS_UPDATE
    }

    private fun findById(id: Int): Notification {
        return notificationRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.NOTIFICATION_NOT_FOUND)
        }
    }
}
