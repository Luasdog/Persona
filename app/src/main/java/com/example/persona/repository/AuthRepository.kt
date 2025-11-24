package com.example.persona.repository

import android.content.Context
import com.example.persona.model.LoginRequest
import com.example.persona.model.RegisterRequest
import com.example.persona.model.response.BaseResponse
import com.example.persona.model.response.LoginResponse
import com.example.persona.network.MockApiService
import com.example.persona.utils.PreferenceManager
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val context: Context
) {
    // 使用模拟API服务
    private val apiService = MockApiService() 
    private val preferenceManager by lazy { PreferenceManager(context) }

    suspend fun login(emailOrUsername: String, password: String): BaseResponse<LoginResponse> {
        val request = LoginRequest(emailOrUsername, password)
        return try {
            val response = apiService.login(request)
            if (response.success && response.data != null) {
                // 保存用户信息和token
                preferenceManager.saveToken(response.data.token)
                preferenceManager.saveUser(response.data.user)
            }
            response
        } catch (e: Exception) {
            BaseResponse(false, e.message ?: "登录失败", null)
        }
    }

    suspend fun register(username: String, email: String, password: String): BaseResponse<LoginResponse> {
        val request = RegisterRequest(username, email, password)
        return try {
            val response = apiService.register(request)
            if (response.success && response.data != null) {
                preferenceManager.saveToken(response.data.token)
                preferenceManager.saveUser(response.data.user)
            }
            response
        } catch (e: Exception) {
            BaseResponse(false, e.message ?: "注册失败", null)
        }
    }

    suspend fun sendVerificationCode(email: String): BaseResponse<Unit> {
        return try {
            apiService.sendVerificationCode(email)
        } catch (e: Exception) {
            BaseResponse(false, e.message ?: "发送验证码失败", null)
        }
    }
    
    suspend fun verifyCode(email: String, code: String): Boolean {
        return try {
            apiService.verifyCode(email, code)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun resetPassword(email: String, newPassword: String): BaseResponse<Unit> {
        return try {
            apiService.resetPassword(email, newPassword)
        } catch (e: Exception) {
            BaseResponse(false, e.message ?: "重置密码失败", null)
        }
    }

    fun logout() {
        preferenceManager.clearUserData()
    }

    fun isLoggedIn(): Boolean {
        return preferenceManager.isLoggedIn()
    }
}