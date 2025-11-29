package com.example.persona.model

data class ChatSession(
    val id: String,
    val contactName: String,
    val lastMessage: String,
    val timestamp: String,
    val avatarUrl: String? = null,
    val unreadCount: Int = 0
)

data class Contact(
    val id: String,
    val name: String,
    val bio: String,
    val avatarUrl: String? = null,
    val isPersona: Boolean = true // 区分是真人还是AI Persona
)

/**
 * 消息类型枚举
 */
enum class MessageType {
    TEXT,           // 纯文本消息
    IMAGE,          // 图片消息（AI生成或用户上传）
    AUDIO,          // 音频消息（AI语音或用户录音）
    MUSIC,          // AI生成的音乐
    VIDEO,          // 视频消息
    FILE            // 文件消息
}

/**
 * 媒体内容数据类
 */
data class MediaContent(
    val url: String? = null,           // 媒体URL（远程或本地）
    val localPath: String? = null,     // 本地文件路径
    val thumbnailUrl: String? = null,  // 缩略图URL
    val duration: Int? = null,         // 音频/视频时长（秒）
    val mimeType: String? = null,      // MIME类型
    val size: Long? = null,            // 文件大小（字节）
    val width: Int? = null,            // 图片/视频宽度
    val height: Int? = null,           // 图片/视频高度
    val metadata: Map<String, String>? = null  // 额外元数据
)

data class Message(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val isTyping: Boolean = false,         // 是否是"正在输入"的临时消息
    val isMarkdown: Boolean = !isFromUser, // AI消息默认支持Markdown渲染
    val messageType: MessageType = MessageType.TEXT,  // 消息类型
    val mediaContent: MediaContent? = null,           // 多媒体内容
    val isGenerating: Boolean = false                 // 是否正在生成中（用于AI生成内容）
)