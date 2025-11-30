package com.example.persona.model

data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val isFriend: Boolean = true  // 标识作者是否为好友，默认为true
)

data class PersonaSettings(
    val id: String,                              // Persona唯一ID
    val name: String,                            // 名字
    val avatarUrl: String? = null,               // 头像URL
    val personality: String,                     // 性格特征
    val backstory: String,                       // 背景故事
    val tone: String,                            // 说话语气
    val interests: List<String> = emptyList(),   // 兴趣爱好
    val strengths: List<String> = emptyList(),   // 擅长领域
    val weaknesses: List<String> = emptyList(),  // 弱点/不擅长的

    // AI能力偏好
    val preferredTextModel: String? = null,      // 优先使用的文本模型
    val preferredImageModel: String? = null,     // 优先使用的图片模型
    val preferredVoice: String? = null,          // 优先使用的语音
    val artStyle: String? = null,                // 艺术风格偏好
    val musicMood: String? = null,               // 音乐情绪偏好

    // 成长记录
    val conversationCount: Int = 0,              // 对话次数
    val lastUpdated: Long = System.currentTimeMillis(), // 最后更新时间
    val createdAt: Long = System.currentTimeMillis(),   // 创建时间
    val growthNotes: String = ""                 // 成长记录/备注
)