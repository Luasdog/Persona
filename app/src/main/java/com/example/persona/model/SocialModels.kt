package com.example.persona.model

data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false
)

data class PersonaSettings(
    val name: String,
    val avatarUrl: String? = null,
    val personality: String, // e.g., "Optimistic", "Cold", "Humorous"
    val backstory: String,
    val tone: String // e.g., "Formal", "Casual"
)