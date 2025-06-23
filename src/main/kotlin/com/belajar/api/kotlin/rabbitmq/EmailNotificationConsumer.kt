package com.belajar.api.kotlin.rabbitmq

import com.belajar.api.kotlin.model.Notification
import com.belajar.api.kotlin.model.UserAccount
import com.belajar.api.kotlin.repository.NotificationRepository

import com.belajar.api.kotlin.service.EmailService
import com.belajar.api.kotlin.entities.notification.EmailNotificationMessage
import com.belajar.api.kotlin.entities.notification.NotificationResponse
import com.belajar.api.kotlin.repository.UserAccountRepository
import com.belajar.api.kotlin.utils.Utilities
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class EmailNotificationConsumer(
    private val emailService: EmailService,
    private val notificationRepository: NotificationRepository,
    private val userAccountRepository: UserAccountRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val utilities: Utilities
) {
    @Value("\${template_api.url-frontend}")
    private lateinit var urlFrontend: String

    @RabbitListener(queues = ["email_notification"])
    fun handleEmailNotification(message: EmailNotificationMessage) {
        val user: UserAccount? = userAccountRepository.findByEmail(message.recipientEmail)

        if (user != null) {
            val logoUrl = "$urlFrontend/img/logo.png"
            val currentYear = LocalDateTime.now().year.toString()

            val templatePath = "templates/email_order_notification.html"

            val variables = mapOf(
                "username" to (user.username),
                "subject" to message.subject,
                "message" to message.message,
                "logoUrl" to logoUrl,
                "year" to currentYear
            )

            emailService.sendTemplateEmail(user, message.subject, templatePath, variables)

            val notification = Notification(
                title = message.subject,
                message = message.message,
                recipient = user,
                isRead = false,
                createdAt = LocalDateTime.now(),
                billId = message.billId,
                customerName = message.customerName
            )
            val savedNotification = notificationRepository.save(notification)

            val hashedId = utilities.encodeId(savedNotification.id!!)
            val hashedBillId = utilities.encodeUuid(savedNotification.billId)
            val response = NotificationResponse(
                id = hashedId,
                title = savedNotification.title,
                message = savedNotification.message,
                billId = hashedBillId,
                customerName = savedNotification.customerName,
                isRead = savedNotification.isRead,
                createdAt = savedNotification.createdAt.toString()
            )

            messagingTemplate.convertAndSendToUser(
                user.username,
                "/queue/notifications/order",
                response
            )

        }
    }

}
