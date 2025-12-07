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
     *
     * @param chatId 聊天ID
     * @param personaContext Persona 上下文（system prompt）
     * @param userMessage 当前用户消息
     * @param includeHistory 是否包含历史消息（默认 true）
     * @param maxHistoryTurns 最多包含多少轮历史对话（默认 10 轮）
     */
    fun streamResponse(
        chatId: String,
        personaContext: String,
        userMessage: String,
        includeHistory: Boolean = true,
        maxHistoryTurns: Int = 10
    ): Flow<String> = flow {
        // 构建完整的消息列表
        val messages = mutableListOf<Pair<String, String>>()

        // 1. 添加 System Prompt（Persona 上下文）
        messages.add("system" to personaContext)

        // 2. 添加历史对话消息（如果启用）
        if (includeHistory) {
            val history = buildConversationHistory(chatId, maxHistoryTurns)
            messages.addAll(history)
        }

        // 3. 添加当前用户消息
        messages.add("user" to userMessage)

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

    /**
     * 构建对话历史
     * 只保留最近 N 轮对话，避免 token 超限
     */
    private fun buildConversationHistory(
        chatId: String,
        maxTurns: Int
    ): List<Pair<String, String>> {
        val allMessages = contactRepository.getMessages(chatId)

        // 过滤掉临时消息和输入指示器
        val validMessages = allMessages.filter {
            it.id != "_ai_temp" &&
            it.id != "_typing" &&
            !it.isTyping &&
            !it.isGenerating &&
            it.text.isNotBlank()
        }

        // 只取最近的 N*2 条消息（N 轮对话 = N 条用户消息 + N 条 AI 消息）
        val recentMessages = validMessages.takeLast(maxTurns * 2)

        // 转换为 LLM 格式
        return recentMessages.map { message ->
            val role = if (message.isFromUser) "user" else "assistant"
            role to message.text
        }
    }
}
