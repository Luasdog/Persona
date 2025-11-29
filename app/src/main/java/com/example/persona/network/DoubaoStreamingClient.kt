package com.example.persona.network

import android.util.Log
import com.example.persona.BuildConfig
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * 豆包流式响应数据模型 (OpenAI-compatible format)
 */
data class DoubaoStreamResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("object") val objectType: String? = null,
    @SerializedName("created") val created: Long? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("choices") val choices: List<DoubaoChoice>? = null,
    @SerializedName("error") val error: DoubaoError? = null
)

data class DoubaoChoice(
    @SerializedName("index") val index: Int,
    @SerializedName("delta") val delta: DoubaoDelta? = null,
    @SerializedName("finish_reason") val finishReason: String? = null
)

data class DoubaoDelta(
    @SerializedName("role") val role: String? = null,
    @SerializedName("content") val content: String? = null
)

data class DoubaoError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("code") val code: String? = null
)

private data class DoubaoMessagePayload(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

private data class DoubaoChatRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<DoubaoMessagePayload>,
    @SerializedName("stream") val stream: Boolean = true
)

/**
 * DoubaoStreamingClient: 根据豆包的 Chunked JSON 格式解析流式响应并 emit content 片段。
 * apiKey: Doubao API Key
 * apiUrl: Doubao 流式 endpoint
 * model: 使用的模型名称
 *
 * 兼容性说明：默认豆包使用 JSON Lines（每个 chunk 为独立 JSON，readLine() 可读到）。
 * 若接入的是 SSE（Server-Sent Events），JSON 可能被包裹在 `data:` 前缀中，
 * 本客户端会自动去除 `data:` 前缀（支持 `data:` 和 `data: ` 两种形式）后再解析 JSON。
 */
class DoubaoStreamingClient(
    private val model: String = "doubao-seed-1-6-251015"
) : LLMStreamingClient, MultimodalAIClient {

    private val apiKey: String = BuildConfig.DOUBAO_API_KEY
    private val apiUrl: String = BuildConfig.DOUBAO_API_URL.ifBlank { DEFAULT_API_URL }

    init {
        require(apiKey.isNotBlank()) { "Doubao API key must not be blank" }
    }

    private val client = OkHttpClient.Builder().build()
    private val gson = Gson()

    override fun streamChatResponse(messages: List<Pair<String, String>>): Flow<String> = flow {
        require(messages.isNotEmpty()) { "messages must contain at least one entry" }

        val payload = DoubaoChatRequest(
            model = model,
            messages = messages.map { (role, text) ->
                DoubaoMessagePayload(role = role, content = text)
            },
            stream = true
        )

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val jsonPayload = gson.toJson(payload)
        val body = jsonPayload.toRequestBody(mediaType)

        val resolvedUrl = apiUrl
        Log.d(TAG, "Doubao call start: url=$resolvedUrl, model=$model, messages=${messages.size}")
        Log.d(TAG, "Request payload: $jsonPayload")
        val request = Request.Builder()
            .url(resolvedUrl)
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            Log.d(TAG, "Doubao response code=${response.code}")
            if (!response.isSuccessful) {
                val errorBody = response.body.string()
                Log.e(TAG, "Doubao API error response: $errorBody")
                throw Exception("Doubao API error [${response.code}]: $errorBody")
            }

            val responseBody = response.body
            val inputStream = responseBody.byteStream()
            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))

            try {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line.isNullOrBlank()) continue

                    // Trim and support SSE 'data:' prefix
                    var raw = line.trim()
                    if (raw.startsWith("data:")) {
                        raw = raw.substringAfter("data:").trim()
                        // Check for stream end marker
                        if (raw == "[DONE]") {
                            Log.d(TAG, "Doubao stream complete [DONE]")
                            break
                        }
                    }

                    if (raw.isBlank()) continue

                    // Parse JSON chunk
                    val chunk = try {
                        gson.fromJson(raw, DoubaoStreamResponse::class.java)
                    } catch (e: Exception) {
                        Log.w(TAG, "Skip invalid chunk: $raw", e)
                        continue
                    }

                    // Check for error in response
                    if (chunk.error != null) {
                        Log.e(TAG, "Doubao API error: ${chunk.error.message}")
                        throw Exception("Doubao API error: ${chunk.error.message ?: "unknown"}")
                    }

                    // Process choices
                    chunk.choices?.forEach { choice ->
                        choice.delta?.content?.let { content ->
                            if (content.isNotEmpty()) {
                                Log.v(TAG, "Streaming chunk length=${content.length}")
                                emit(content)
                            }
                        }

                        // Check if this is the final chunk
                        if (choice.finishReason != null) {
                            Log.d(TAG, "Doubao stream finished with reason: ${choice.finishReason}")
                            return@flow
                        }
                    }
                }
            } finally {
                reader.close()
                responseBody.close()
                Log.d(TAG, "Doubao stream connection closed")
            }
        }
    }.flowOn(Dispatchers.IO)

    // ===== MultimodalAIClient 接口实现 =====

    override fun generateText(messages: List<Pair<String, String>>): Flow<String> {
        // 复用现有的文本生成能力
        return streamChatResponse(messages)
    }

    override suspend fun generateImage(
        prompt: String,
        style: String?,
        size: String
    ): ImageGenerationResult {
        // 豆包Seed模型暂不支持图片生成
        Log.w(TAG, "Image generation not supported by Doubao Seed model")
        return ImageGenerationResult(
            success = false,
            errorMessage = "当前模型不支持图片生成功能，请切换到支持图片生成的模型"
        )
    }

    override suspend fun generateMusic(
        prompt: String,
        duration: Int,
        mood: String?
    ): MusicGenerationResult {
        // 豆包Seed模型暂不支持音乐生成
        Log.w(TAG, "Music generation not supported by Doubao Seed model")
        return MusicGenerationResult(
            success = false,
            errorMessage = "当前模型不支持音乐生成功能，请切换到支持音乐生成的模型"
        )
    }

    override suspend fun textToSpeech(
        text: String,
        voice: String?,
        speed: Float
    ): AudioGenerationResult {
        // 豆包Seed模型暂不支持语音合成
        Log.w(TAG, "Text-to-speech not supported by Doubao Seed model")
        return AudioGenerationResult(
            success = false,
            errorMessage = "当前模型不支持语音合成功能，请切换到支持语音合成的模型"
        )
    }

    override fun getCapabilities(): Set<AICapability> {
        // 豆包Seed模型只支持文本生成
        return setOf(AICapability.TEXT_GENERATION)
    }

    companion object {
        private const val TAG = "DoubaoStreaming"
        private const val DEFAULT_API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"
    }
}
