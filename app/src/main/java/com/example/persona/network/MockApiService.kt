package com.example.persona.network

import com.example.persona.model.LoginRequest
import com.example.persona.model.RegisterRequest
import com.example.persona.model.User
import com.example.persona.model.response.BaseResponse
import com.example.persona.model.response.LoginResponse
import kotlinx.coroutines.delay

class MockApiService {
    // 模拟延迟
    private val delayTime = 1000L

    suspend fun login(request: LoginRequest): BaseResponse<LoginResponse> {
        delay(delayTime)

        // 简单的模拟逻辑
        if (request.email == "test@example.com" && request.password == "123456") {
            val user = User(
                id = "1",
                username = "测试用户",
                email = request.email,
                avatar = null,
                createdAt = System.currentTimeMillis()
            )
            return BaseResponse(
                success = true,
                message = "登录成功",
                data = LoginResponse(user = user, token = "mock_token_123456")
            )
        }
        return BaseResponse(success = false, message = "邮箱或密码错误")
    }

    suspend fun register(request: RegisterRequest): BaseResponse<LoginResponse> {
        delay(delayTime)

        // 简单的模拟逻辑
        if (request.email.contains("@")) {
            val user = User(
                id = System.currentTimeMillis().toString(),
                username = request.username,
                email = request.email,
                avatar = null,
                createdAt = System.currentTimeMillis()
            )
            return BaseResponse(
                success = true,
                message = "注册成功",
                data = LoginResponse(user = user, token = "mock_token_" + System.currentTimeMillis())
            )
        }
        return BaseResponse(success = false, message = "邮箱格式不正确")
    }
}