package com.belajar.api.kotlin.entities.user

data class GoogleUserResponse(
    val email: String,
    val name: String,
    var username: String? = null,
    var tokenAccess: String? = null
)
