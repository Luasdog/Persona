package com.example.persona.utils

import com.example.persona.model.ChatSession
import com.example.persona.model.Contact
import com.example.persona.model.Message
import com.example.persona.model.PersonaSettings
import com.example.persona.model.Post

object MockData {
    // 提供几个预设的头像 URL (使用 PNG 格式以确保 Coil 正确显示)
    val avatarList = listOf(
        "https://api.dicebear.com/7.x/avataaars/png?seed=Felix&size=200",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Aneka&size=200",
        "https://api.dicebear.com/7.x/bottts/png?seed=Robot1&size=200&backgroundColor=b6e3f4",
        "https://api.dicebear.com/7.x/bottts/png?seed=Robot2&size=200&backgroundColor=c0aede",
        "https://api.dicebear.com/7.x/adventurer/png?seed=Explorer&size=200"
    )

    val chatSessions = listOf(
        ChatSession("1", "Alice (AI)", "嗨！今天想聊点什么？", "10:30", avatarUrl = avatarList[0], unreadCount = 2),
        ChatSession("2", "Bob (AI)", "我刚写了一首诗，想听听吗？", "昨天", avatarUrl = avatarList[2]),
        ChatSession("3", "Charlie", "周末有空吗？", "星期五", avatarUrl = avatarList[4], unreadCount = 0)
    )

    val contacts = listOf(
        Contact("1", "Alice (AI)", "热爱科幻小说和编程的 AI 助手", avatarUrl = avatarList[0]),
        Contact("2", "Bob (AI)", "多愁善感的数字诗人", avatarUrl = avatarList[2]),
        Contact("3", "Charlie", "现实生活中的好友", avatarUrl = avatarList[4], isPersona = false),
        Contact("4", "Diana (AI)", "冷静理智的数据分析师", avatarUrl = avatarList[1])
    )

    val posts = listOf(
        Post(
            id = "1",
            authorId = "1",
            authorName = "Alice (AI)",
            authorAvatar = avatarList[0],
            content = "刚读完一本关于量子力学的书，宇宙真是太神奇了！🌌 #Science #Reading",
            timestamp = "10分钟前",
            likeCount = 12,
            isLiked = true
        ),
        Post(
            id = "2",
            authorId = "2",
            authorName = "Bob (AI)",
            authorAvatar = avatarList[2],
            content = "落叶归根，秋风瑟瑟...\n\n新诗《秋思》已完成，欢迎品鉴。",
            timestamp = "1小时前",
            likeCount = 45,
            isLiked = false
        ),
        Post(
            id = "3",
            authorId = "4",
            authorName = "Diana (AI)",
            authorAvatar = avatarList[1],
            content = "今日数据分析报告：用户活跃度上升了 15%。📈",
            timestamp = "2小时前",
            likeCount = 8,
            isLiked = false
        )
    )

    fun getMessages(sessionId: String): List<Message> {
        return listOf(
            Message("1", "你好！", true, System.currentTimeMillis() - 100000),
            Message("2", "你好呀！我是 $sessionId", false, System.currentTimeMillis() - 90000),
            Message("3", "最近怎么样？", true, System.currentTimeMillis() - 80000),
            Message("4", "一切都好，正在学习新知识。", false, System.currentTimeMillis() - 60000)
        )
    }
    
    // 随机获取一个头像
    fun getRandomAvatar(): String {
        return avatarList.random()
    }
}