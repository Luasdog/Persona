package com.example.persona.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.persona.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val registerStateRaw by viewModel.registerState.collectAsState()
    val verificationCodeState by viewModel.verificationCodeState.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    // 倒计时状态
    var countdown by remember { mutableStateOf(0) }
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    // 监听注册状态变化
    LaunchedEffect(registerStateRaw) {
        val registerState = registerStateRaw
        if (registerState?.success == true) {
            onRegisterSuccess()
        } else if (registerState?.success == false) {
            errorMessage = registerState.message
        }
    }
    
    // 监听验证码发送状态
    LaunchedEffect(verificationCodeState) {
        verificationCodeState?.message?.let {
             if (verificationCodeState?.success == false) {
                 errorMessage = it
             } else {
                 // 验证码发送成功，可以在这里显示Toast，这里简单处理
             }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "注册", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("邮箱") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 验证码行
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("验证码") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (email.contains("@")) {
                        viewModel.sendVerificationCode(email)
                        countdown = 60
                    } else {
                        errorMessage = "请输入正确的邮箱"
                    }
                },
                enabled = !isLoading && countdown == 0 && email.isNotEmpty()
            ) {
                Text(if (countdown > 0) "${countdown}s" else "发送验证码")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            placeholder = { Text("8位以上，含大小写字母和数字") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("确认密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        val passwordsMatch = password == confirmPassword
        val isFormValid = username.isNotEmpty() && email.isNotEmpty() &&
                password.isNotEmpty() && passwordsMatch && verificationCode.isNotEmpty()

        Button(
            onClick = {
                if (!passwordsMatch) {
                    errorMessage = "两次输入的密码不一致"
                } else {
                    viewModel.register(username, email, password, verificationCode)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && isFormValid
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("注册")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("已有账号，去登录")
        }
    }
}