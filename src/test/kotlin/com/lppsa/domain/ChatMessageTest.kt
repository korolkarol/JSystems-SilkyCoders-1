package com.lppsa.domain

import com.lppsa.domain.model.ChatMessage
import com.lppsa.domain.model.MessageRole
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChatMessageTest {
    @Test
    fun `ChatMessage holds all fields`() {
        val msg = ChatMessage(
            id = "m1",
            sessionId = "s1",
            role = MessageRole.USER,
            content = "Pytanie",
            createdAt = "2026-03-27T10:01:00",
        )
        assertEquals("m1", msg.id)
        assertEquals("s1", msg.sessionId)
        assertEquals(MessageRole.USER, msg.role)
        assertEquals("Pytanie", msg.content)
    }

    @Test
    fun `MessageRole has USER and ASSISTANT`() {
        assertEquals(2, MessageRole.entries.size)
        assertEquals(MessageRole.USER, MessageRole.valueOf("USER"))
        assertEquals(MessageRole.ASSISTANT, MessageRole.valueOf("ASSISTANT"))
    }
}
