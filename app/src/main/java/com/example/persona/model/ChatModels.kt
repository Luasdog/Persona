package com.example.persona.model

data class ChatSession(
    val id: String,
    val contactName: String,
    val lastMessage: String,
    val timestamp: String,
    val avatarUrl: String? = null,
    val unreadCount: Int = 0
)

data class Contact(
    val id: String,
    val name: String,
    val bio: String,
    val avatarUrl: String? = null,
    val isPersona: Boolean = true // 区分是真人还是AI Persona
)

data class Message(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long
)