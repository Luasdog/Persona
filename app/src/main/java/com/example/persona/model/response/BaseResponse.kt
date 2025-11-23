package com.example.persona.model.response

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)