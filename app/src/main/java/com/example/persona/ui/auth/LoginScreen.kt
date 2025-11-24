package com.example.persona.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.persona.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val loginState by viewModel.loginState.collectAsState()

    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // 监听登录状态变化
    LaunchedEffect(loginState) {
        val currentLoginState = loginState
        if (currentLoginState?.success == true) {
            // 登录成功，导航到主页
            onLoginSuccess()
        } else if (currentLoginState?.success == false) {
            errorMessage = currentLoginState.message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "登录", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        OutlinedTextField(
            value = emailOrUsername,
            onValueChange = { emailOrUsername = it },
            label = { Text("邮箱或用户名") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(4.dp))

        // 忘记密码按钮
        TextButton(
            onClick = onNavigateToForgotPassword,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("忘记密码？")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.login(emailOrUsername, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && emailOrUsername.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("登录")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("注册新账号")
        }
    }
}
