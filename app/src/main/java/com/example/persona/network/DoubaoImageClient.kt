package com.example.persona.network

import android.util.Log
import com.example.persona.BuildConfig
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * 豆包图片生成请求数据模型
 */
private data class DoubaoImageRequest(
    @SerializedName("model") val model: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("response_format") val responseFormat: String = "url",
    @SerializedName("size") val size: String = "1024x1024",
    @SerializedName("guidance_scale") val guidanceScale: Int = 3,
    @SerializedName("watermark") val watermark: Boolean = true
)

/**
 * 豆包图片生成响应数据模型
 */
private data class DoubaoImageResponse(
    @SerializedName("data") val data: List<DoubaoImageData>? = null,
    @SerializedName("error") val error: DoubaoImageError? = null,
    @SerializedName("created") val created: Long? = null
)

private data class DoubaoImageData(
    @SerializedName("url") val url: String? = null,
    @SerializedName("b64_json") val b64Json: String? = null
)

private data class DoubaoImageError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("code") val code: String? = null
)

/**
 * 豆包图片生成客户端
 * 使用 doubao-seedream-3-0-t2i-250415 模型进行文生图
 *
 * API 文档参考：https://ark.cn-beijing.volces.com/api/v3/images/generations
 */
class DoubaoImageClient(
    private val model: String = "doubao-seedream-3-0-t2i-250415"
) : MultimodalAIClient {

    private val TAG = "DoubaoImage"

    private val apiKey: String = BuildConfig.DOUBAO_API_KEY
    private val apiUrl: String = "https://ark.cn-beijing.volces.com/api/v3/images/generations"

    init {
        require(apiKey.isNotBlank()) { "Doubao API key must not be blank" }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    override fun generateText(messages: List<Pair<String, String>>): Flow<String> {
        throw UnsupportedOperationException("DoubaoImageClient does not support text generation")
    }

    override suspend fun generateImage(
        prompt: String,
        style: String?,
        size: String
    ): ImageGenerationResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating image with Doubao: prompt='$prompt', style='$style', size='$size'")

            // 如果提供了风格，将其融入到 prompt 中
            val enhancedPrompt = if (style != null && style.isNotBlank()) {
                "$prompt，风格：$style"
            } else {
                prompt
            }

            val request = DoubaoImageRequest(
                model = model,
                prompt = enhancedPrompt,
                responseFormat = "url",
                size = size,
                guidanceScale = 5,  // 平衡值，既保证准确性又有创意
                watermark = false   // 关闭水印
            )

            val jsonPayload = gson.toJson(request)
            Log.d(TAG, "Request payload: $jsonPayload")

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = jsonPayload.toRequestBody(mediaType)

            val httpRequest = Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Response code=${response.code}, body=$responseBody")

                if (!response.isSuccessful) {
                    Log.e(TAG, "Doubao Image API error response: $responseBody")
                    val errorResponse = try {
                        gson.fromJson(responseBody, DoubaoImageResponse::class.java)
                    } catch (e: Exception) {
                        null
                    }

                    val errorMessage = errorResponse?.error?.message
                        ?: "图片生成失败 [${response.code}]: $responseBody"

                    return@withContext ImageGenerationResult(
                        success = false,
                        errorMessage = errorMessage
                    )
                }

                val imageResponse = gson.fromJson(responseBody, DoubaoImageResponse::class.java)

                if (imageResponse.data.isNullOrEmpty()) {
                    Log.e(TAG, "No image data in response")
                    return@withContext ImageGenerationResult(
                        success = false,
                        errorMessage = "图片生成成功但未返回图片URL"
                    )
                }

                val imageUrl = imageResponse.data[0].url

                if (imageUrl.isNullOrBlank()) {
                    Log.e(TAG, "Image URL is null or blank")
                    return@withContext ImageGenerationResult(
                        success = false,
                        errorMessage = "图片生成成功但URL为空"
                    )
                }

                Log.d(TAG, "Image generated successfully: $imageUrl")

                ImageGenerationResult(
                    success = true,
                    imageUrl = imageUrl,
                    width = parseSizeWidth(size),
                    height = parseSizeHeight(size)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating image", e)
            ImageGenerationResult(
                success = false,
                errorMessage = "图片生成异常: ${e.message}"
            )
        }
    }

    override suspend fun generateMusic(
        prompt: String,
        duration: Int,
        mood: String?
    ): MusicGenerationResult {
        throw UnsupportedOperationException("DoubaoImageClient does not support music generation")
    }

    override suspend fun textToSpeech(
        text: String,
        voice: String?,
        speed: Float
    ): AudioGenerationResult {
        throw UnsupportedOperationException("DoubaoImageClient does not support speech synthesis")
    }

    override fun getCapabilities(): Set<AICapability> {
        return setOf(AICapability.IMAGE_GENERATION)
    }

    /**
     * 从尺寸字符串解析宽度 (e.g., "1024x1024" -> 1024)
     */
    private fun parseSizeWidth(size: String): Int? {
        return try {
            size.split("x")[0].toInt()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从尺寸字符串解析高度 (e.g., "1024x1024" -> 1024)
     */
    private fun parseSizeHeight(size: String): Int? {
        return try {
            size.split("x")[1].toInt()
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "DoubaoImage"
    }
}

