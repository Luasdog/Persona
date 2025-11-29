package com.example.persona.repository

import com.example.persona.network.LLMStreamingClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMChatRepository @Inject constructor(
    private val llmClient: LLMStreamingClient,
    private val contactRepository: ContactRepository
) {
    /**
     * 对外提供的流式响应：会在收到每个片段时 emit，并且在内部将片段保存在 ContactRepository 的临时消息中，
     * 最终把完整回复写入 repository。
     */
    fun streamResponse(chatId: String, personaContext: String, userMessage: String): Flow<String> = flow {
        val messages = listOf(
            "system" to personaContext,
            "user" to userMessage
        )
        val accumulated = StringBuilder()
        // 订阅 llmClient 的流
        llmClient.streamChatResponse(messages).collect { chunk ->
            // 逐片段发出给 UI
            emit(chunk)
            accumulated.append(chunk)
            // 将临时片段写入 repository，供 UI 持续读取显示
            contactRepository.upsertTempAiMessage(chatId, accumulated.toString())
        }
        // 完成后，将最终文本写入 repository（直接调用 suspend 函数，确保写入完成后 flow 才结束）
        val finalText = accumulated.toString()
        contactRepository.finalizeAiMessage(chatId, finalText)
    }
}
