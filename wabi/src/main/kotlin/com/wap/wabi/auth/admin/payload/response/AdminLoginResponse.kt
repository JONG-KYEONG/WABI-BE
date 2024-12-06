package com.wap.wabi.auth.admin.payload.response

data class AdminLoginResponse(
    val name: String,
    val accessToken: String,
    val refreshToken : String,
    val role: String,
)
