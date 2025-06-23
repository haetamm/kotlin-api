package com.belajar.api.kotlin.websocket

import com.belajar.api.kotlin.model.UserAccount
import java.security.Principal

class CustomPrincipal(
    private val username: String,
    val user: UserAccount
) : Principal {
    override fun getName(): String = username
}
