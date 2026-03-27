package com.lppsa.infrastructure

import com.lppsa.domain.model.ChatMessage
import com.lppsa.domain.model.MessageRole
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import com.lppsa.infrastructure.persistence.JdbcChatRepository
import com.lppsa.infrastructure.persistence.JdbcSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class JdbcChatRepositoryTest {

    @Autowired
    lateinit var jdbc: JdbcTemplate

    @Autowired
    lateinit var chatRepository: JdbcChatRepository

    @Autowired
    lateinit var sessionRepository: JdbcSessionRepository

    @BeforeEach
    fun cleanUp() {
        jdbc.execute("DELETE FROM chat_messages")
        jdbc.execute("DELETE FROM sessions")
    }

    private suspend fun createSession(id: String = "session-1") {
        sessionRepository.save(
            Session(
                id = id,
                requestType = RequestType.REKLAMACJA,
                productName = "Buty",
                purchaseDate = "2025-06-01",
                description = "Podeszwa odkleiła się",
                createdAt = "2026-03-27T10:00:00",
            )
        )
    }

    @Test
    fun `save and findBySessionId returns message`() = runTest {
        createSession()
        val msg = ChatMessage(
            id = "msg-1",
            sessionId = "session-1",
            role = MessageRole.USER,
            content = "Czy mam szansę na zwrot?",
            createdAt = "2026-03-27T10:01:00",
        )
        chatRepository.save(msg)
        val messages = chatRepository.findBySessionId("session-1")
        assertEquals(1, messages.size)
        assertEquals(msg.id, messages[0].id)
        assertEquals(msg.content, messages[0].content)
        assertEquals(MessageRole.USER, messages[0].role)
    }

    @Test
    fun `findBySessionId returns empty list when no messages`() = runTest {
        createSession()
        val messages = chatRepository.findBySessionId("session-1")
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `multiple messages returned ordered by created_at`() = runTest {
        createSession()
        chatRepository.save(
            ChatMessage(
                id = "m1",
                sessionId = "session-1",
                role = MessageRole.USER,
                content = "Pytanie",
                createdAt = "2026-03-27T10:01:00",
            )
        )
        chatRepository.save(
            ChatMessage(
                id = "m2",
                sessionId = "session-1",
                role = MessageRole.ASSISTANT,
                content = "Odpowiedź",
                createdAt = "2026-03-27T10:02:00",
            )
        )
        val messages = chatRepository.findBySessionId("session-1")
        assertEquals(2, messages.size)
        assertEquals("m1", messages[0].id)
        assertEquals("m2", messages[1].id)
    }
}
