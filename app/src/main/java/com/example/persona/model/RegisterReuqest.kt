package com.example.persona.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)