package com.lppsa.domain.port

import com.lppsa.domain.model.ChatMessage

interface ChatRepository {
    suspend fun save(message: ChatMessage)
    suspend fun findBySessionId(sessionId: String): List<ChatMessage>
}
