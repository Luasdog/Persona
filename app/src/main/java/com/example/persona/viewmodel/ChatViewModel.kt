package com.example.persona.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.model.Message
import com.example.persona.repository.ContactRepository
import com.example.persona.repository.LLMChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val llmChatRepository: LLMChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _contactName = MutableStateFlow("")
    val contactName: StateFlow<String> = _contactName.asStateFlow()

    private var streamingJob: Job? = null

    init {
        loadMessages()
        loadContactInfo()
        markAsRead()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            // Initially load messages
            refreshMessages()
        }
    }
    
    private fun loadContactInfo() {
        val contact = contactRepository.getContactById(chatId)
        contact?.let {
            _contactName.value = it.name
        }
    }
    
    private fun markAsRead() {
        viewModelScope.launch {
            contactRepository.markAsRead(chatId)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // 1. User sends message (save immediately)
            contactRepository.sendMessage(chatId, text)
            refreshMessages()

            // 2. Cancel previous streaming if any
            streamingJob?.cancel()

            // 3. Build persona context from contact info
            val contact = contactRepository.getContactById(chatId)
            val personaContext = if (contact != null && contact.isPersona) {
                """你是 ${contact.name}，一个AI数字人格。
                |性格特征：${contact.bio}
                |请以这个角色的身份，保持角色设定，友好地与用户对话。
                |回复内容可以使用Markdown格式来增强表达效果。
                """.trimMargin()
            } else {
                "你是一个友好且专业的AI助手。回复可以使用Markdown格式。"
            }

            // 4. 显示"正在输入"指示器
            contactRepository.addTypingIndicator(chatId)
            refreshMessages()

            // 5. Start new streaming job to fetch LLM response
            streamingJob = launch {
                try {
                    // Start streaming; LLMChatRepository will persist partial chunks into ContactRepository
                    llmChatRepository.streamResponse(chatId, personaContext, text).collect { chunk ->
                        // Refresh messages to pick up the persisted temp message written by repository
                        refreshMessages()
                    }

                    // When stream completes, repository has already finalized the AI message. Refresh to pick it up.
                    refreshMessages()
                } catch (e: Exception) {
                    // 移除输入指示器
                    contactRepository.removeTypingIndicator(chatId)
                    // 显示错误消息
                    contactRepository.receiveAiResponse(
                        chatId,
                        "抱歉，发生了一些错误：${e.message ?: "未知错误"}。请稍后重试。"
                    )
                    refreshMessages()
                }
            }
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
    }

    private suspend fun refreshMessages() {
        // Use toList() to create a new List instance, forcing StateFlow to emit a change
        _messages.value = contactRepository.getMessages(chatId).toList()
        markAsRead() // Ensure we mark as read when seeing new messages while open
    }
}