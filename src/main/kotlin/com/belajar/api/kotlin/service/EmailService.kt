package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.model.UserAccount

interface EmailService {

    fun sendEmail(to: String, subject: String, htmlContent: String)

    fun sendTemplateEmail(
        user: UserAccount,
        subject: String,
        templatePath: String,
        variables: Map<String, String>
    )
}