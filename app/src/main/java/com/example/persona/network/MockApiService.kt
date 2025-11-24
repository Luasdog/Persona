package com.example.persona.network

import com.example.persona.model.LoginRequest
import com.example.persona.model.RegisterRequest
import com.example.persona.model.User
import com.example.persona.model.response.BaseResponse
import com.example.persona.model.response.LoginResponse
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class MockApiService {
    private val delayTime = 800L

    companion object {
        // 模拟数据库：邮箱 -> 用户
        private val userDatabase = ConcurrentHashMap<String, User>()
        // 模拟数据库：用户名 -> 邮箱 (用于用户名登录查找)
        private val usernameMap = ConcurrentHashMap<String, String>()
        // 模拟数据库：邮箱 -> 密码
        private val passwordDatabase = ConcurrentHashMap<String, String>()
        // 模拟验证码：邮箱 -> 验证码
        private val verificationCodes = ConcurrentHashMap<String, String>()

        init {
            // 初始化默认用户
            val defaultEmail = "test@example.com"
            val defaultUser = User(
                id = "1",
                username = "测试用户",
                email = defaultEmail,
                avatar = null,
                createdAt = System.currentTimeMillis()
            )
            userDatabase[defaultEmail] = defaultUser
            usernameMap["testuser"] = defaultEmail
            passwordDatabase[defaultEmail] = "123456"
        }
    }

    suspend fun login(request: LoginRequest): BaseResponse<LoginResponse> {
        delay(delayTime)

        val identifier = request.email // 这里可能是邮箱也可能是用户名
        val password = request.password

        // 1. 尝试作为邮箱查找
        var email = identifier
        if (!userDatabase.containsKey(identifier)) {
            // 2. 尝试作为用户名查找
            if (usernameMap.containsKey(identifier)) {
                email = usernameMap[identifier]!!
            } else {
                return BaseResponse(success = false, message = "用户不存在")
            }
        }

        // 3. 验证密码
        if (passwordDatabase[email] == password) {
            val user = userDatabase[email]!!
            return BaseResponse(
                success = true,
                message = "登录成功",
                data = LoginResponse(user = user, token = "mock_token_${System.currentTimeMillis()}")
            )
        } else {
            return BaseResponse(success = false, message = "密码错误")
        }
    }

    suspend fun register(request: RegisterRequest): BaseResponse<LoginResponse> {
        delay(delayTime)

        if (userDatabase.containsKey(request.email)) {
            return BaseResponse(success = false, message = "该邮箱已被注册")
        }
        if (usernameMap.containsKey(request.username)) {
            return BaseResponse(success = false, message = "用户名已存在")
        }

        val newUser = User(
            id = System.currentTimeMillis().toString(),
            username = request.username,
            email = request.email,
            avatar = null,
            createdAt = System.currentTimeMillis()
        )

        userDatabase[request.email] = newUser
        usernameMap[request.username] = request.email
        passwordDatabase[request.email] = request.password

        return BaseResponse(
            success = true,
            message = "注册成功",
            data = LoginResponse(user = newUser, token = "mock_token_${System.currentTimeMillis()}")
        )
    }

    suspend fun sendVerificationCode(email: String): BaseResponse<Unit> {
        delay(delayTime)
        // 生成6位数字验证码
        val code = (100000..999999).random().toString()
        verificationCodes[email] = code
        // 在实际开发中这里会调用邮件服务，这里我们在Log或UI提示中显示（为了演示方便，我们假设用户输入总是正确的，或者固定为123456方便测试）
        // 此次为了模拟真实体验，我们将验证码固定为 "123456"，并在控制台打印
        println("Mock Email Service: Verification code for $email is 123456")
        
        // 也可以存储真正的随机码，但在测试时你需要知道它。为了方便，我们不仅存随机码，还允许 "123456" 作为万能码通过验证逻辑（见 verifyCode）
        return BaseResponse(success = true, message = "验证码已发送（模拟：123456）", data = null)
    }

    suspend fun verifyCode(email: String, code: String): Boolean {
        delay(500)
        // 允许 "123456" 或者 真实生成的码
        return code == "123456" || verificationCodes[email] == code
    }

    suspend fun resetPassword(email: String, newPassword: String): BaseResponse<Unit> {
        delay(delayTime)
        if (!userDatabase.containsKey(email)) {
            return BaseResponse(success = false, message = "该邮箱未注册")
        }
        passwordDatabase[email] = newPassword
        return BaseResponse(success = true, message = "密码重置成功", data = null)
    }
}