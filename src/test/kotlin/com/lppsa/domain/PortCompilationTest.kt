package com.lppsa.domain

import com.lppsa.domain.model.*
import com.lppsa.domain.port.ChatRepository
import com.lppsa.domain.port.EvaluationPort
import com.lppsa.domain.port.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.jupiter.api.Test

class PortCompilationTest {
    @Test
    fun `SessionRepository can be implemented anonymously`() {
        val repo = object : SessionRepository {
            override suspend fun save(session: Session) {}
            override suspend fun findById(id: String): Session? = null
            override suspend fun updateDecision(id: String, decision: Decision) {}
        }
        // just verifies it compiles
    }

    @Test
    fun `ChatRepository can be implemented anonymously`() {
        val repo = object : ChatRepository {
            override suspend fun save(message: ChatMessage) {}
            override suspend fun findBySessionId(sessionId: String): List<ChatMessage> = emptyList()
        }
    }

    @Test
    fun `EvaluationPort can be implemented anonymously`() {
        val port = object : EvaluationPort {
            override fun evaluate(
                requestType: RequestType,
                productName: String,
                purchaseDate: String,
                description: String,
                imageBytes: ByteArray,
                imageMimeType: String,
            ): Flow<String> = emptyFlow()

            override fun chat(
                session: Session,
                history: List<ChatMessage>,
                userMessage: String,
            ): Flow<String> = emptyFlow()
        }
    }
}
