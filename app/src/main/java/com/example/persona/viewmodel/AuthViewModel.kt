package com.example.persona.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.model.response.BaseResponse
import com.example.persona.model.response.LoginResponse
import com.example.persona.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<BaseResponse<LoginResponse>?>(null)
    val loginState: StateFlow<BaseResponse<LoginResponse>?> = _loginState

    private val _registerState = MutableStateFlow<BaseResponse<LoginResponse>?>(null)
    val registerState: StateFlow<BaseResponse<LoginResponse>?> = _registerState

    private val _resetPasswordState = MutableStateFlow<BaseResponse<Unit>?>(null)
    val resetPasswordState: StateFlow<BaseResponse<Unit>?> = _resetPasswordState

    private val _verificationCodeState = MutableStateFlow<BaseResponse<Unit>?>(null)
    val verificationCodeState: StateFlow<BaseResponse<Unit>?> = _verificationCodeState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(emailOrUsername: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = authRepository.login(emailOrUsername, password)
            _isLoading.value = false
        }
    }

    fun register(username: String, email: String, password: String, code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // 1. 验证码校验
            val isCodeValid = authRepository.verifyCode(email, code)
            if (!isCodeValid) {
                _registerState.value = BaseResponse(false, "验证码错误或已过期", null)
                _isLoading.value = false
                return@launch
            }

            // 2. 密码强度校验
            if (!isPasswordStrong(password)) {
                _registerState.value = BaseResponse(false, "密码需包含大小写字母和数字，且长度不少于8位", null)
                _isLoading.value = false
                return@launch
            }

            // 3. 注册请求
            _registerState.value = authRepository.register(username, email, password)
            _isLoading.value = false
        }
    }

    fun sendVerificationCode(email: String) {
        viewModelScope.launch {
            // 简单的邮箱格式校验
            if (!email.contains("@")) {
                _verificationCodeState.value = BaseResponse(false, "请输入有效的邮箱地址", null)
                return@launch
            }
            _isLoading.value = true
            _verificationCodeState.value = authRepository.sendVerificationCode(email)
            _isLoading.value = false
        }
    }

    fun resetPassword(email: String, newPassword: String, code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // 1. 验证码校验
            val isCodeValid = authRepository.verifyCode(email, code)
            if (!isCodeValid) {
                _resetPasswordState.value = BaseResponse(false, "验证码错误", null)
                _isLoading.value = false
                return@launch
            }
            
            // 2. 密码强度校验
            if (!isPasswordStrong(newPassword)) {
                _resetPasswordState.value = BaseResponse(false, "密码需包含大小写字母和数字，且长度不少于8位", null)
                _isLoading.value = false
                return@launch
            }

            // 3. 重置密码请求
            _resetPasswordState.value = authRepository.resetPassword(email, newPassword)
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
        _loginState.value = null
        _registerState.value = null
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    // 简单的密码强度校验：8位以上，包含大小写字母和数字
    private fun isPasswordStrong(password: String): Boolean {
        if (password.length < 8) return false
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUpperCase && hasLowerCase && hasDigit
    }
    
    // 重置状态，避免重复提示
    fun clearStates() {
        _loginState.value = null
        _registerState.value = null
        _resetPasswordState.value = null
        _verificationCodeState.value = null
    }
}