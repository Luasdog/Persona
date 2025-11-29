package com.example.persona.repository

import com.example.persona.model.ChatSession
import com.example.persona.model.Contact
import com.example.persona.model.Message
import com.example.persona.utils.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor() {

    // 使用 MutableList 存储运行时的联系人数据，初始值来自 MockData
    private val _contacts = MutableStateFlow(MockData.contacts)
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // 存储聊天会话
    private val _chatSessions = MutableStateFlow(MockData.chatSessions)
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    // 简单的内存消息存储: ChatId -> List<Message>
    private val _messages = mutableMapOf<String, MutableList<Message>>()

    init {
        // 初始化 Mock 消息
        MockData.chatSessions.forEach { session ->
            _messages[session.id] = MockData.getMessages(session.id).toMutableList()
        }
    }

    fun addContact(contact: Contact) {
        _contacts.update { currentList ->
            listOf(contact) + currentList // 新增的放前面
        }
        
        // 同时创建一个空的聊天会话
        val newSession = ChatSession(
            id = contact.id,
            contactName = contact.name,
            lastMessage = "开始聊天吧！",
            timestamp = "刚刚",
            avatarUrl = contact.avatarUrl,
            unreadCount = 0
        )
        
        _chatSessions.update { currentList ->
            listOf(newSession) + currentList
        }
        
        // 初始化消息列表
        _messages[contact.id] = mutableListOf()
    }

    fun getMessages(chatId: String): List<Message> {
        return _messages[chatId] ?: emptyList()
    }

    fun sendMessage(chatId: String, text: String) {
        val newMessage = Message(
            id = System.currentTimeMillis().toString(),
            text = text,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        saveMessage(chatId, newMessage)
    }

    /**
     * 保存最终的 AI 回复文本（非临时），用于流式完成后的写入
     */
    fun finalizeAiMessage(chatId: String, finalText: String) {
        val list = _messages.getOrPut(chatId) { mutableListOf() }

        // 移除临时消息和输入指示器
        list.removeAll { it.id == "_ai_temp" || it.id == "_typing" }

        val newMessage = Message(
            id = System.currentTimeMillis().toString(),
            text = finalText,
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            isTyping = false,
            isMarkdown = true
        )
        saveMessage(chatId, newMessage)
    }

    /**
     * 添加"正在输入"指示器
     */
    fun addTypingIndicator(chatId: String) {
        val list = _messages.getOrPut(chatId) { mutableListOf() }
        // 移除已存在的输入指示器
        list.removeAll { it.id == "_typing" }

        val typingMsg = Message(
            id = "_typing",
            text = "",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            isTyping = true
        )
        list.add(typingMsg)
    }

    /**
     * 移除"正在输入"指示器
     */
    fun removeTypingIndicator(chatId: String) {
        val list = _messages.getOrPut(chatId) { mutableListOf() }
        list.removeAll { it.id == "_typing" }
    }

    /**
     * 在生成过程中持续写入或更新一个临时的 AI 消息（id = "_ai_temp"），方便 UI 增量展示
     */
    fun upsertTempAiMessage(chatId: String, partialText: String) {
        val list = _messages.getOrPut(chatId) { mutableListOf() }

        // 移除输入指示器（开始接收内容）
        list.removeAll { it.id == "_typing" }

        val index = list.indexOfFirst { it.id == "_ai_temp" }
        if (index >= 0) {
            list[index] = list[index].copy(
                text = partialText,
                timestamp = System.currentTimeMillis(),
                isTyping = false  // 已经开始接收内容，不再是输入状态
            )
        } else {
            val tempMsg = Message(
                id = "_ai_temp",
                text = partialText,
                isFromUser = false,
                timestamp = System.currentTimeMillis(),
                isTyping = false,
                isMarkdown = true
            )
            list.add(tempMsg)
        }

        // Update session last message (do not increment unread for temp)
        _chatSessions.update { sessions ->
            sessions.map { session ->
                if (session.id == chatId) {
                    val preview = if (partialText.length > 30) {
                        partialText.take(27) + "..."
                    } else {
                        partialText
                    }
                    session.copy(
                        lastMessage = preview,
                        timestamp = "刚刚"
                    )
                } else session
            }
        }
    }

    fun receiveAiResponse(chatId: String, text: String) {
        // 保持向后兼容：内部调用 finalizeAiMessage
        finalizeAiMessage(chatId, text)
    }

    fun markAsRead(chatId: String) {
        _chatSessions.update { sessions ->
            sessions.map { session ->
                if (session.id == chatId) {
                    session.copy(unreadCount = 0)
                } else {
                    session
                }
            }
        }
    }

    private fun saveMessage(chatId: String, message: Message) {
        if (!_messages.containsKey(chatId)) {
            _messages[chatId] = mutableListOf()
        }
        _messages[chatId]?.add(message)
        
        // 更新会话列表的最新消息
        _chatSessions.update { sessions ->
            sessions.map { session ->
                if (session.id == chatId) {
                    val unreadIncrement = if (!message.isFromUser) 1 else 0 // AI 发送时增加未读
                    session.copy(
                        lastMessage = message.text, 
                        timestamp = "刚刚",
                        unreadCount = session.unreadCount + unreadIncrement
                    )
                } else {
                    session
                }
            }
        }
    }
    
    fun getContactById(id: String): Contact? {
        return _contacts.value.find { it.id == id }
    }
}