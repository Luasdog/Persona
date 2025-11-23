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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = authRepository.login(email, password)
            _isLoading.value = false
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _registerState.value = authRepository.register(username, email, password)
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
}