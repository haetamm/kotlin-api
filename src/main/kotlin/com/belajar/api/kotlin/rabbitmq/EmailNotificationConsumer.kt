package com.belajar.api.kotlin.rabbitmq

import com.belajar.api.kotlin.entities.notification.EmailNotificationMessage
import com.belajar.api.kotlin.repository.UserAccountRepository
import com.belajar.api.kotlin.service.EmailService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class EmailNotificationConsumer(
    private val emailService: EmailService,
    private val userAccountRepository: UserAccountRepository,
) {

    @Value("\${template_api.url-frontend}")
    private lateinit var urlFrontend: String

    private val log = LoggerFactory.getLogger(EmailNotificationConsumer::class.java)

    @RabbitListener(queues = ["email_notification"])
    fun handleEmailNotification(message: EmailNotificationMessage) {
        log.info("📥 [RabbitMQ] Received email for: ${message.recipientEmail}")

        val user = userAccountRepository.findByEmail(message.recipientEmail.trim())
        if (user == null) {
            log.warn("❌ User not found: ${message.recipientEmail}")
            return
        }

        try {
            val variables = mapOf(
                "username" to user.username,
                "subject" to message.subject,
                "message" to message.message,
                "logoUrl" to "$urlFrontend/img/logo.png",
                "year" to LocalDateTime.now().year.toString()
            )

            val templatePath = message.template.ifBlank { "templates/email_order_notification.html" }

            emailService.sendTemplateEmail(user, message.subject, templatePath, variables)
            log.info("✅ Email sent to ${user.email}")
        } catch (e: Exception) {
            log.error("🔥 Failed to send email to ${user.email}: ${e.message}", e)
        }
    }


}
