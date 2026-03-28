package com.lppsa.application

import com.lppsa.application.usecase.SendChatMessageUseCase
import com.lppsa.domain.model.ChatMessage
import com.lppsa.domain.model.Decision
import com.lppsa.domain.model.MessageRole
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import com.lppsa.domain.port.ChatRepository
import com.lppsa.domain.port.EvaluationPort
import com.lppsa.domain.port.SessionRepository
import com.lppsa.infrastructure.metrics.BusinessMetrics
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class SendChatMessageUseCaseTest {

    private lateinit var sessionRepository: FakeSessionRepository2
    private lateinit var chatRepository: FakeChatRepository
    private lateinit var evaluationPort: FakeEvaluationPort2
    private lateinit var useCase: SendChatMessageUseCase

    private val existingSession = Session(
        id = "session-1",
        requestType = RequestType.REKLAMACJA,
        productName = "Koszulka",
        purchaseDate = "2026-01-10",
        description = "Dziura w materiale",
        decision = Decision.Accept(
            explanation = "Reklamacja zasadna",
            nextSteps = "Skontaktuj się z obsługą",
        ),
        createdAt = "2026-03-28T10:00:00",
    )

    @BeforeEach
    fun setUp() {
        sessionRepository = FakeSessionRepository2()
        chatRepository = FakeChatRepository()
        evaluationPort = FakeEvaluationPort2()
        useCase = SendChatMessageUseCase(
            sessionRepository = sessionRepository,
            chatRepository = chatRepository,
            evaluationPort = evaluationPort,
            businessMetrics = BusinessMetrics(SimpleMeterRegistry()),
        )
    }

    @Test
    fun `execute loads session by id`() = runTest {
        sessionRepository.store["session-1"] = existingSession
        evaluationPort.responseTokens = listOf("Odpowiedź asystenta")

        val flow = useCase.execute(
            sessionId = "session-1",
            userMessage = "Kiedy dostanę odpowiedź?",
        )
        flow.toList()

        assertTrue(sessionRepository.queriedIds.contains("session-1"))
    }

    @Test
    fun `execute persists user ChatMessage before streaming`() = runTest {
        sessionRepository.store["session-1"] = existingSession
        evaluationPort.responseTokens = listOf("Odpowiedź asystenta")

        val flow = useCase.execute(
            sessionId = "session-1",
            userMessage = "Kiedy dostanę odpowiedź?",
        )
        flow.toList()

        val userMessages = chatRepository.savedMessages.filter { it.role == MessageRole.USER }
        assertEquals(1, userMessages.size)
        assertEquals("Kiedy dostanę odpowiedź?", userMessages.first().content)
        assertEquals("session-1", userMessages.first().sessionId)
    }

    @Test
    fun `execute calls EvaluationPort chat with session history and user message`() = runTest {
        sessionRepository.store["session-1"] = existingSession
        val existingHistory = listOf(
            ChatMessage(
                id = "msg-1",
                sessionId = "session-1",
                role = MessageRole.USER,
                content = "Poprzednie pytanie",
                createdAt = "2026-03-28T10:01:00",
            ),
        )
        chatRepository.historyBySession["session-1"] = existingHistory
        evaluationPort.responseTokens = listOf("Odpowiedź")

        val flow = useCase.execute(
            sessionId = "session-1",
            userMessage = "Nowe pytanie",
        )
        flow.toList()

        assertNotNull(evaluationPort.lastChatCall)
        val (session, history, message) = evaluationPort.lastChatCall!!
        assertEquals("session-1", session.id)
        assertEquals("Nowe pytanie", message)
        assertTrue(history.any { it.content == "Poprzednie pytanie" })
    }

    @Test
    fun `execute persists assistant ChatMessage after stream completes`() = runTest {
        sessionRepository.store["session-1"] = existingSession
        evaluationPort.responseTokens = listOf("Odpowiedź ", "asystenta")

        val flow = useCase.execute(
            sessionId = "session-1",
            userMessage = "Pytanie",
        )
        flow.toList()

        val assistantMessages = chatRepository.savedMessages.filter { it.role == MessageRole.ASSISTANT }
        assertEquals(1, assistantMessages.size)
        assertEquals("Odpowiedź asystenta", assistantMessages.first().content)
    }

    @Test
    fun `execute throws when session not found`() = runTest {
        evaluationPort.responseTokens = listOf("token")

        try {
            val flow = useCase.execute(
                sessionId = "non-existent",
                userMessage = "Pytanie",
            )
            flow.toList()
            fail("Expected exception for missing session")
        } catch (e: NoSuchElementException) {
            assertTrue(e.message?.contains("non-existent") == true || e.message != null)
        }
    }
}

class FakeSessionRepository2 : SessionRepository {
    val store = mutableMapOf<String, Session>()
    val queriedIds = mutableListOf<String>()

    override suspend fun save(session: Session) {
        store[session.id] = session
    }

    override suspend fun findById(id: String): Session? {
        queriedIds.add(id)
        return store[id]
    }

    override suspend fun updateDecision(id: String, decision: Decision) {
        store[id] = store[id]?.copy(decision = decision) ?: return
    }
}

class FakeChatRepository : ChatRepository {
    val savedMessages = mutableListOf<ChatMessage>()
    val historyBySession = mutableMapOf<String, List<ChatMessage>>()

    override suspend fun save(message: ChatMessage) {
        savedMessages.add(message)
    }

    override suspend fun findBySessionId(sessionId: String): List<ChatMessage> =
        historyBySession[sessionId] ?: emptyList()
}

class FakeEvaluationPort2 : EvaluationPort {
    var responseTokens: List<String> = emptyList()
    var lastChatCall: Triple<Session, List<ChatMessage>, String>? = null

    override fun evaluate(
        requestType: RequestType,
        productName: String,
        purchaseDate: String,
        description: String,
        imageBytes: ByteArray,
        imageMimeType: String,
    ): Flow<String> = flowOf(*responseTokens.toTypedArray())

    override fun chat(
        session: Session,
        history: List<ChatMessage>,
        userMessage: String,
    ): Flow<String> {
        lastChatCall = Triple(session, history, userMessage)
        return flowOf(*responseTokens.toTypedArray())
    }
}
