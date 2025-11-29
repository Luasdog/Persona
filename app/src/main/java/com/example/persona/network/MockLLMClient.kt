package com.example.persona.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockLLMClient : LLMStreamingClient {
    override fun streamChatResponse(messages: List<Pair<String, String>>): Flow<String> = flow {
        // 简单模拟：将回应拆分为三部分，间隔500ms发出
        val userMsg = messages.lastOrNull { it.first == "user" }?.second ?: ""
        val personaPrefix = "这是由 Persona 根据设定生成的回复：\n\n"
        val full = when {
            userMsg.contains("你好") -> personaPrefix + "你好！很高兴和你聊天。\n\n**关于我**：我是你的数字人格。"
            userMsg.contains("名字") -> personaPrefix + "你可以叫我 PersonaBot。"
            else -> personaPrefix + "我收到了：\n> $userMsg\n\n接下来我会继续生成更详细的内容...\n\n```kotlin\nval x = 1\nprintln(x)\n```"
        }

        // 分段发出（简单按句子分割）
        val parts = full.split(Regex("(?<=\\.\\s|\\n\\n)")) // 尝试在句末和空行处拆分
        for (part in parts) {
            emit(part)
            delay(400)
        }

        // 完成后不再发出，Flow 完成表示结束
    }
}

