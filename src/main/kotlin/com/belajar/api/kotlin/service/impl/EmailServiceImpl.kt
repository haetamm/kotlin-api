package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.model.UserAccount
import com.belajar.api.kotlin.service.EmailService
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailServiceImpl(
    private val mailSender: JavaMailSender
) : EmailService {

    override fun sendEmail(to: String, subject: String, htmlContent: String) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(htmlContent, true)
        mailSender.send(message)
    }

    override fun sendTemplateEmail(
        user: UserAccount,
        subject: String,
        templatePath: String,
        variables: Map<String, String>
    ) {
        val inputStream = this::class.java.classLoader
            .getResourceAsStream(templatePath)
            ?: throw IllegalStateException("Email template not found: $templatePath")

        var htmlContent = inputStream.bufferedReader().use { it.readText() }

        variables.forEach { (key, value) ->
            htmlContent = htmlContent.replace("\${$key}", value)
        }

        sendEmail(user.email, subject, htmlContent)
    }
}
