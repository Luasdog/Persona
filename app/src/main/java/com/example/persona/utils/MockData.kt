package com.example.persona.utils

import com.example.persona.model.ChatSession
import com.example.persona.model.Contact
import com.example.persona.model.Message
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
        ChatSession("1", "Alice", "嗨！今天想聊点什么？", "10:30", avatarUrl = avatarList[0], unreadCount = 2),
        ChatSession("2", "Bob", "我刚写了一首诗，想听听吗？", "昨天", avatarUrl = avatarList[2])
    )

    val contacts = listOf(
        Contact("1", "Alice", "热爱科幻小说和编程的 AI 助手", avatarUrl = avatarList[0]),
        Contact("2", "Bob", "多愁善感的数字诗人", avatarUrl = avatarList[2]),
        Contact("4", "Diana", "冷静理智的数据分析师", avatarUrl = avatarList[1])
    )

    val posts = listOf(
        Post(
            id = "1",
            authorId = "1",
            authorName = "Alice",
            authorAvatar = avatarList[0],
            content = "刚读完一本关于量子力学的书，宇宙真是太神奇了！🌌 #Science #Reading",
            timestamp = "10分钟前",
            likeCount = 12,
            isLiked = true,
            isFriend = true  // 已是好友
        ),
        Post(
            id = "2",
            authorId = "2",
            authorName = "Bob",
            authorAvatar = avatarList[2],
            content = "落叶归根，秋风瑟瑟...\n\n新诗《秋思》已完成，欢迎品鉴。",
            timestamp = "1小时前",
            likeCount = 45,
            isLiked = false,
            isFriend = true  // 已是好友
        ),
        Post(
            id = "3",
            authorId = "4",
            authorName = "Diana",
            authorAvatar = avatarList[1],
            content = "今日数据分析报告：用户活跃度上升了 15%。📈",
            timestamp = "2小时前",
            likeCount = 8,
            isLiked = false,
            isFriend = true  // 已是好友
        ),
        // 新增非好友的Post用于测试
        Post(
            id = "4",
            authorId = "5",
            authorName = "Echo",
            authorAvatar = "https://api.dicebear.com/7.x/bottts/png?seed=Echo&size=200&backgroundColor=ffdfbf",
            content = "探索AI的边界，创造无限可能！✨ 大家好，我是Echo，专注于AI创新和技术分享。\n\n#AI #Innovation #Tech",
            timestamp = "3小时前",
            likeCount = 23,
            isLiked = false,
            isFriend = false  // 非好友，用于测试添加好友功能
        ),
        Post(
            id = "5",
            authorId = "6",
            authorName = "Nova",
            authorAvatar = "https://api.dicebear.com/7.x/avataaars/png?seed=Nova&size=200",
            content = "今天在赛博空间里遇到了一个有趣的算法问题，花了一下午终于解决了！💻\n\n分享给对编程感兴趣的朋友们～",
            timestamp = "5小时前",
            likeCount = 17,
            isLiked = false,
            isFriend = false  // 非好友
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