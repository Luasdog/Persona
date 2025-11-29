package com.example.persona.network

import kotlinx.coroutines.flow.Flow

/**
 * 多模态AI客户端统一接口
 * 支持文本、图片、音频、音乐等多种模态的生成
 */
interface MultimodalAIClient {

    /**
     * 文本生成（流式）
     * @param messages 对话历史
     * @return 流式文本内容
     */
    fun generateText(messages: List<Pair<String, String>>): Flow<String>

    /**
     * 文生图（Text-to-Image）
     * @param prompt 图片描述文本
     * @param style 艺术风格（可选）
     * @param size 图片尺寸（可选）
     * @return 生成的图片URL
     */
    suspend fun generateImage(
        prompt: String,
        style: String? = null,
        size: String = "1024x1024"
    ): ImageGenerationResult

    /**
     * 文生音乐（Text-to-Music）
     * @param prompt 音乐描述
     * @param duration 时长（秒）
     * @param mood 情绪/风格
     * @return 生成的音乐URL
     */
    suspend fun generateMusic(
        prompt: String,
        duration: Int = 30,
        mood: String? = null
    ): MusicGenerationResult

    /**
     * 文本转语音（Text-to-Speech）
     * @param text 要转换的文本
     * @param voice 语音类型/音色
     * @param speed 语速
     * @return 生成的音频URL
     */
    suspend fun textToSpeech(
        text: String,
        voice: String? = null,
        speed: Float = 1.0f
    ): AudioGenerationResult

    /**
     * 检查该客户端支持的能力
     */
    fun getCapabilities(): Set<AICapability>
}

/**
 * AI能力枚举
 */
enum class AICapability {
    TEXT_GENERATION,      // 文本生成
    IMAGE_GENERATION,     // 图片生成
    MUSIC_GENERATION,     // 音乐生成
    SPEECH_SYNTHESIS,     // 语音合成
    SPEECH_RECOGNITION,   // 语音识别
    VIDEO_GENERATION      // 视频生成（预留）
}

/**
 * 图片生成结果
 */
data class ImageGenerationResult(
    val success: Boolean,
    val imageUrl: String? = null,
    val thumbnailUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val errorMessage: String? = null
)

/**
 * 音乐生成结果
 */
data class MusicGenerationResult(
    val success: Boolean,
    val musicUrl: String? = null,
    val duration: Int? = null,
    val format: String? = null,
    val errorMessage: String? = null
)

/**
 * 音频生成结果
 */
data class AudioGenerationResult(
    val success: Boolean,
    val audioUrl: String? = null,
    val duration: Int? = null,
    val format: String? = null,
    val errorMessage: String? = null
)

