package com.example.persona.network

import android.util.Log
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI模型管理器
 * 负责管理和调度多个AI模型，根据需求选择合适的模型
 */
@Singleton
class AIModelManager @Inject constructor() {

    private val TAG = "AIModelManager"

    // 注册的AI客户端列表
    private val clients = mutableMapOf<String, MultimodalAIClient>()

    // 当前默认的文本生成客户端
    private var defaultTextClient: MultimodalAIClient? = null

    // 当前默认的图片生成客户端
    private var defaultImageClient: MultimodalAIClient? = null

    // 当前默认的音乐生成客户端
    private var defaultMusicClient: MultimodalAIClient? = null

    // 当前默认的语音合成客户端
    private var defaultSpeechClient: MultimodalAIClient? = null

    /**
     * 注册AI客户端
     */
    fun registerClient(name: String, client: MultimodalAIClient) {
        clients[name] = client
        Log.d(TAG, "Registered AI client: $name with capabilities: ${client.getCapabilities()}")

        // 自动设置默认客户端
        val capabilities = client.getCapabilities()
        if (AICapability.TEXT_GENERATION in capabilities && defaultTextClient == null) {
            defaultTextClient = client
            Log.d(TAG, "Set $name as default text generation client")
        }
        if (AICapability.IMAGE_GENERATION in capabilities && defaultImageClient == null) {
            defaultImageClient = client
            Log.d(TAG, "Set $name as default image generation client")
        }
        if (AICapability.MUSIC_GENERATION in capabilities && defaultMusicClient == null) {
            defaultMusicClient = client
            Log.d(TAG, "Set $name as default music generation client")
        }
        if (AICapability.SPEECH_SYNTHESIS in capabilities && defaultSpeechClient == null) {
            defaultSpeechClient = client
            Log.d(TAG, "Set $name as default speech synthesis client")
        }
    }

    /**
     * 获取指定名称的客户端
     */
    fun getClient(name: String): MultimodalAIClient? {
        return clients[name]
    }

    /**
     * 生成文本（流式）
     */
    fun generateText(
        messages: List<Pair<String, String>>,
        clientName: String? = null
    ): Flow<String> {
        val client = clientName?.let { clients[it] } ?: defaultTextClient
        requireNotNull(client) { "No text generation client available" }

        if (AICapability.TEXT_GENERATION !in client.getCapabilities()) {
            throw UnsupportedOperationException("Selected client does not support text generation")
        }

        return client.generateText(messages)
    }

    /**
     * 生成图片
     */
    suspend fun generateImage(
        prompt: String,
        style: String? = null,
        size: String = "1024x1024",
        clientName: String? = null
    ): ImageGenerationResult {
        val client = clientName?.let { clients[it] } ?: defaultImageClient

        if (client == null) {
            Log.w(TAG, "No image generation client available")
            return ImageGenerationResult(
                success = false,
                errorMessage = "没有可用的图片生成模型，请先配置图片生成客户端"
            )
        }

        if (AICapability.IMAGE_GENERATION !in client.getCapabilities()) {
            Log.w(TAG, "Selected client does not support image generation")
            return ImageGenerationResult(
                success = false,
                errorMessage = "所选模型不支持图片生成功能"
            )
        }

        return client.generateImage(prompt, style, size)
    }

    /**
     * 生成音乐
     */
    suspend fun generateMusic(
        prompt: String,
        duration: Int = 30,
        mood: String? = null,
        clientName: String? = null
    ): MusicGenerationResult {
        val client = clientName?.let { clients[it] } ?: defaultMusicClient

        if (client == null) {
            Log.w(TAG, "No music generation client available")
            return MusicGenerationResult(
                success = false,
                errorMessage = "没有可用的音乐生成模型，请先配置音乐生成客户端"
            )
        }

        if (AICapability.MUSIC_GENERATION !in client.getCapabilities()) {
            Log.w(TAG, "Selected client does not support music generation")
            return MusicGenerationResult(
                success = false,
                errorMessage = "所选模型不支持音乐生成功能"
            )
        }

        return client.generateMusic(prompt, duration, mood)
    }

    /**
     * 文本转语音
     */
    suspend fun textToSpeech(
        text: String,
        voice: String? = null,
        speed: Float = 1.0f,
        clientName: String? = null
    ): AudioGenerationResult {
        val client = clientName?.let { clients[it] } ?: defaultSpeechClient

        if (client == null) {
            Log.w(TAG, "No speech synthesis client available")
            return AudioGenerationResult(
                success = false,
                errorMessage = "没有可用的语音合成模型，请先配置语音合成客户端"
            )
        }

        if (AICapability.SPEECH_SYNTHESIS !in client.getCapabilities()) {
            Log.w(TAG, "Selected client does not support speech synthesis")
            return AudioGenerationResult(
                success = false,
                errorMessage = "所选模型不支持语音合成功能"
            )
        }

        return client.textToSpeech(text, voice, speed)
    }

    /**
     * 获取所有已注册的客户端
     */
    fun getAllClients(): Map<String, MultimodalAIClient> {
        return clients.toMap()
    }

    /**
     * 检查是否有支持特定能力的客户端
     */
    fun hasCapability(capability: AICapability): Boolean {
        return clients.values.any { capability in it.getCapabilities() }
    }

    /**
     * 获取支持特定能力的所有客户端名称
     */
    fun getClientsWithCapability(capability: AICapability): List<String> {
        return clients.filterValues { capability in it.getCapabilities() }.keys.toList()
    }
}

