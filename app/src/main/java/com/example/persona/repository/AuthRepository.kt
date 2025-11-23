package com.example.persona.repository

import android.content.Context
import com.example.persona.model.LoginRequest
import com.example.persona.model.RegisterRequest
import com.example.persona.model.response.BaseResponse
import com.example.persona.model.response.LoginResponse
import com.example.persona.network.RetrofitClient
import com.example.persona.utils.PreferenceManager
import javax.inject.Inject
import com.example.persona.network.MockApiService

class AuthRepository @Inject constructor(
    private val context: Context
) {
//    private val apiService = RetrofitClient.apiService
    private val apiService = MockApiService() // 使用模拟API服务代替实际网络请求
    private val preferenceManager by lazy { PreferenceManager(context) }

    suspend fun login(email: String, password: String): BaseResponse<LoginResponse> {
        val request = LoginRequest(email, password)
        return try {
            val response = apiService.login(request)
            if (response.success && response.data != null) {
                // 保存用户信息和token
                preferenceManager.saveToken(response.data.token)
                preferenceManager.saveUser(response.data.user)
            }
            response
        } catch (e: Exception) {
            // 网络请求失败时的处理
            BaseResponse(false, e.message ?: "登录失败", null)
        }
    }

    suspend fun register(username: String, email: String, password: String): BaseResponse<LoginResponse> {
        val request = RegisterRequest(username, email, password)
        return try {
            val response = apiService.register(request)
            if (response.success && response.data != null) {
                // 保存用户信息和token
                preferenceManager.saveToken(response.data.token)
                preferenceManager.saveUser(response.data.user)
            }
            response
        } catch (e: Exception) {
            // 网络请求失败时的处理
            BaseResponse(false, e.message ?: "注册失败", null)
        }
    }

    fun logout() {
        preferenceManager.clearUserData()
    }

    fun isLoggedIn(): Boolean {
        return preferenceManager.isLoggedIn()
    }
}