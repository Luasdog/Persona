package com.example.persona.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.model.Message
import com.example.persona.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _contactName = MutableStateFlow("")
    val contactName: StateFlow<String> = _contactName.asStateFlow()

    init {
        loadMessages()
        loadContactInfo()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            // Initially load messages
            _messages.value = contactRepository.getMessages(chatId)
            // In a real app, we would collect a Flow from repository
        }
    }
    
    private fun loadContactInfo() {
        val contact = contactRepository.getContactById(chatId)
        contact?.let {
            _contactName.value = it.name
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // 1. User sends message
            contactRepository.sendMessage(chatId, text)
            refreshMessages() // Refresh local state

            // 2. Simulate AI thinking delay
            delay(1500)

            // 3. AI responds (Simple Logic)
            val responseText = generateAiResponse(text)
            contactRepository.receiveAiResponse(chatId, responseText)
            refreshMessages()
        }
    }
    
    private suspend fun refreshMessages() {
        _messages.value = contactRepository.getMessages(chatId)
    }

    // Mock AI Logic - This will be replaced by LLM API later
    private fun generateAiResponse(userText: String): String {
        return when {
            userText.contains("你好") -> "你好呀！很高兴见到你。我是你的专属 Persona。"
            userText.contains("名字") -> "你可以叫我 ${_contactName.value}。"
            userText.contains("诗") -> "明月几时有，把酒问青天..."
            else -> "这很有趣，请继续告诉我更多关于 \"$userText\" 的事情。"
        }
    }
}