package com.lppsa.infrastructure.persistence

import com.lppsa.domain.model.ChatMessage
import com.lppsa.domain.model.MessageRole
import com.lppsa.domain.port.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class JdbcChatRepository(private val jdbc: JdbcTemplate) : ChatRepository {

    override suspend fun save(message: ChatMessage): Unit = withContext(Dispatchers.IO) {
        jdbc.update(
            "INSERT INTO chat_messages (id, session_id, role, content, created_at) VALUES (?, ?, ?, ?, ?)",
            message.id,
            message.sessionId,
            message.role.name,
            message.content,
            message.createdAt,
        )
    }

    override suspend fun findBySessionId(sessionId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        jdbc.query(
            "SELECT * FROM chat_messages WHERE session_id = ? ORDER BY created_at",
            { rs, _ ->
                ChatMessage(
                    id = rs.getString("id"),
                    sessionId = rs.getString("session_id"),
                    role = MessageRole.valueOf(rs.getString("role")),
                    content = rs.getString("content"),
                    createdAt = rs.getString("created_at"),
                )
            },
            sessionId,
        )
    }
}
