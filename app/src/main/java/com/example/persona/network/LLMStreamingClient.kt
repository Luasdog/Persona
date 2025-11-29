package com.example.persona.network

import kotlinx.coroutines.flow.Flow

/**
 * LLMStreamingClient: 提供流式获取大模型回复的接口。
 * 实现应返回一个按顺序发出文本片段的 Flow，并在完成时正常完成流。
 */
interface LLMStreamingClient {
    /**
     * 将 messages（包含 system/persona 上下文与 user 提问）发送给远端大模型，返回一个 Flow<String>
     * 每个元素为增量的文本片段（应该保持顺序），最终流完成代表生成结束。
     * messages: List of Pair(role, content) e.g. listOf("system" to "...", "user" to "...")
     */
    fun streamChatResponse(messages: List<Pair<String, String>>): Flow<String>
}

