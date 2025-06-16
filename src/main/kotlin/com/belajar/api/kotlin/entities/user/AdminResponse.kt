package com.belajar.api.kotlin.entities.user

data class AdminResponse(
  var id: String,
  var username: String,
  var email: String,
  val isEnable: Boolean,
  val roles: List<String>,
  val createdAt: String,
  val updatedAt: String,

)
