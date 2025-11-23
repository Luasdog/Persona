package com.example.persona.model.response

import com.example.persona.model.User

data class LoginResponse(
    val user: User,
    val token: String
)