package com.example.persona.network

import com.example.persona.model.LoginRequest
import com.example.persona.model.RegisterRequest
import com.example.persona.model.response.BaseResponse
import com.example.persona.model.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): BaseResponse<LoginResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): BaseResponse<LoginResponse>
}