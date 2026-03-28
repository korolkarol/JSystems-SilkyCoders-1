package com.lppsa.application.usecase

import com.lppsa.domain.model.ChatMessage
import com.lppsa.domain.model.MessageRole
import com.lppsa.domain.port.ChatRepository
import com.lppsa.domain.port.EvaluationPort
import com.lppsa.domain.port.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.util.UUID

class SendChatMessageUseCase(
    private val sessionRepository: SessionRepository,
    private val chatRepository: ChatRepository,
    private val evaluationPort: EvaluationPort,
) {
    fun execute(
        sessionId: String,
        userMessage: String,
    ): Flow<String> = flow {
        val session = sessionRepository.findById(sessionId)
            ?: throw NoSuchElementException("Session not found: $sessionId")

        val history = chatRepository.findBySessionId(sessionId)

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = MessageRole.USER,
            content = userMessage,
            createdAt = LocalDateTime.now().toString(),
        )
        chatRepository.save(userMsg)

        val sb = StringBuilder()
        evaluationPort.chat(
            session = session,
            history = history,
            userMessage = userMessage,
        ).collect { token ->
            sb.append(token)
            emit(token)
        }

        val assistantMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = MessageRole.ASSISTANT,
            content = sb.toString(),
            createdAt = LocalDateTime.now().toString(),
        )
        chatRepository.save(assistantMsg)
    }
}
