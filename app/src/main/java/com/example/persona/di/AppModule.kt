package com.example.persona.di

import android.content.Context
import android.util.Log
import com.example.persona.BuildConfig
import com.example.persona.network.DoubaoStreamingClient
import com.example.persona.network.LLMStreamingClient
import com.example.persona.network.MockLLMClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val TAG = "PersonaDI"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideLLMStreamingClient(): LLMStreamingClient {
        // Prefer BuildConfig values (injected at build time via buildConfigField)
        val douKeyFromBuild = BuildConfig.DOUBAO_API_KEY
        val douUrlFromBuild = BuildConfig.DOUBAO_API_URL

        val douKey = if (douKeyFromBuild.isNotBlank()) douKeyFromBuild else (System.getenv("DOUBAO_API_KEY") ?: "")
        val douUrl = if (douUrlFromBuild.isNotBlank()) douUrlFromBuild else (System.getenv("DOUBAO_API_URL") ?: "")

        val client: LLMStreamingClient = if (douKey.isNotBlank()) {
            DoubaoStreamingClient()
        } else {
            MockLLMClient()
        }

        when (client) {
            is DoubaoStreamingClient -> Log.d(TAG, "Using DoubaoStreamingClient; key=${if (douKey.isNotBlank()) "[REDACTED]" else "(none)"}, url=$douUrl")
            is MockLLMClient -> Log.d(TAG, "Using MockLLMClient (no real API key found)")
            else -> Log.d(TAG, "Using unknown LLM client: ${client::class.java.name}")
        }

        return client
    }

    @Provides
    @Singleton
    fun provideAIModelManager(): com.example.persona.network.AIModelManager {
        val manager = com.example.persona.network.AIModelManager()

        // 注册豆包客户端（当前主要的文本生成模型）
        val douKeyFromBuild = BuildConfig.DOUBAO_API_KEY
        val douKey = if (douKeyFromBuild.isNotBlank()) douKeyFromBuild else (System.getenv("DOUBAO_API_KEY") ?: "")

        if (douKey.isNotBlank()) {
            // 注册豆包文本生成客户端
            val doubaoClient = DoubaoStreamingClient()
            manager.registerClient("doubao", doubaoClient)
            Log.d(TAG, "Registered Doubao client for text generation")

            // 注册豆包图片生成客户端
            val doubaoImageClient = com.example.persona.network.DoubaoImageClient()
            manager.registerClient("doubao-image", doubaoImageClient)
            Log.d(TAG, "Registered Doubao Image client for image generation")
        }

        // TODO: 注册其他AI客户端
        // 示例：当配置了OpenAI API密钥时
        // val openaiKey = BuildConfig.OPENAI_API_KEY
        // if (openaiKey.isNotBlank()) {
        //     val openaiClient = OpenAIMultimodalClient(openaiKey)
        //     manager.registerClient("openai", openaiClient)
        //     Log.d(TAG, "Registered OpenAI client for text, image, and speech")
        // }

        // 示例：配置Stable Diffusion用于图片生成
        // val sdKey = BuildConfig.SD_API_KEY
        // if (sdKey.isNotBlank()) {
        //     val sdClient = StableDiffusionClient(sdKey)
        //     manager.registerClient("stable-diffusion", sdClient)
        //     Log.d(TAG, "Registered Stable Diffusion client for image generation")
        // }

        Log.d(TAG, "AIModelManager initialized with ${manager.getAllClients().size} client(s)")
        Log.d(TAG, "Available capabilities: TEXT=${manager.hasCapability(com.example.persona.network.AICapability.TEXT_GENERATION)}, " +
                "IMAGE=${manager.hasCapability(com.example.persona.network.AICapability.IMAGE_GENERATION)}, " +
                "MUSIC=${manager.hasCapability(com.example.persona.network.AICapability.MUSIC_GENERATION)}, " +
                "SPEECH=${manager.hasCapability(com.example.persona.network.AICapability.SPEECH_SYNTHESIS)}")

        return manager
    }
}