package com.example.persona.utils

import com.example.persona.model.ChatSession
import com.example.persona.model.Contact
import com.example.persona.model.Message

object MockData {
    val chatSessions = listOf(
        ChatSession("1", "Alice (AI)", "嗨！今天想聊点什么？", "10:30", unreadCount = 2),
        ChatSession("2", "Bob (AI)", "我刚写了一首诗，想听听吗？", "昨天"),
        ChatSession("3", "Charlie", "周末有空吗？", "星期五", unreadCount = 0)
    )

    val contacts = listOf(
        Contact("1", "Alice (AI)", "热爱科幻小说和编程的 AI 助手"),
        Contact("2", "Bob (AI)", "多愁善感的数字诗人"),
        Contact("3", "Charlie", "现实生活中的好友", isPersona = false),
        Contact("4", "Diana (AI)", "冷静理智的数据分析师")
    )
    
    fun getMessages(sessionId: String): List<Message> {
        return listOf(
            Message("1", "你好！", true, System.currentTimeMillis() - 100000),
            Message("2", "你好呀！我是 $sessionId", false, System.currentTimeMillis() - 90000),
            Message("3", "最近怎么样？", true, System.currentTimeMillis() - 80000),
            Message("4", "一切都好，正在学习新知识。", false, System.currentTimeMillis() - 60000)
        )
    }
}