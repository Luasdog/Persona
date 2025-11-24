package com.example.persona.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.persona.ui.auth.ForgotPasswordScreen
import com.example.persona.ui.auth.LoginScreen
import com.example.persona.ui.auth.RegisterScreen
import com.example.persona.ui.main.MainScreen
import com.example.persona.ui.theme.PersonaTheme
import com.example.persona.viewmodel.AuthViewModel

@Composable
fun AppContent() {
    val authViewModel: AuthViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(Screen.Login) }
    var isUserLoggedIn by remember { mutableStateOf(authViewModel.isLoggedIn()) }

    val loginState by authViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState?.success == true) {
            isUserLoggedIn = true
        }
    }

    PersonaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isUserLoggedIn) {
                MainScreen(onLogout = {
                    authViewModel.logout()
                    isUserLoggedIn = false
                    currentScreen = Screen.Login
                })
            } else {
                when (currentScreen) {
                    Screen.Login -> {
                        LoginScreen(
                            onLoginSuccess = {
                                // handled by LaunchedEffect observing state
                            },
                            onNavigateToRegister = { currentScreen = Screen.Register },
                            onNavigateToForgotPassword = { currentScreen = Screen.ForgotPassword }
                        )
                    }
                    Screen.Register -> {
                        RegisterScreen(
                            onRegisterSuccess = {
                                currentScreen = Screen.Login
                            },
                            onNavigateToLogin = { currentScreen = Screen.Login }
                        )
                    }
                    Screen.ForgotPassword -> {
                        ForgotPasswordScreen(
                            onNavigateToLogin = { currentScreen = Screen.Login }
                        )
                    }
                    else -> {
                        currentScreen = Screen.Login
                    }
                }
            }
        }
    }
}

enum class Screen {
    Login,
    Register,
    ForgotPassword,
    Main
}
