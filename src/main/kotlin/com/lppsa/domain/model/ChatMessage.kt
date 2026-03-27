package com.lppsa.domain.model

enum class MessageRole { USER, ASSISTANT }

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val createdAt: String,
)
