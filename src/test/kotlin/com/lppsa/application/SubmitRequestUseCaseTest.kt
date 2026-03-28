package com.lppsa.application

import com.lppsa.application.usecase.SubmitCommand
import com.lppsa.application.usecase.SubmitRequestUseCase
import com.lppsa.domain.model.ChatMessage
import com.lppsa.domain.model.Decision
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import com.lppsa.domain.port.EvaluationPort
import com.lppsa.domain.port.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SubmitRequestUseCaseTest {

    private lateinit var sessionRepository: FakeSessionRepository
    private lateinit var evaluationPort: FakeEvaluationPort
    private lateinit var useCase: SubmitRequestUseCase

    @BeforeEach
    fun setUp() {
        sessionRepository = FakeSessionRepository()
        evaluationPort = FakeEvaluationPort()
        useCase = SubmitRequestUseCase(
            sessionRepository = sessionRepository,
            evaluationPort = evaluationPort,
        )
    }

    private val defaultCommand = SubmitCommand(
        requestType = RequestType.REKLAMACJA,
        productName = "Koszulka",
        purchaseDate = "2026-01-10",
        description = "Dziura w materiale",
        imageBytes = ByteArray(10),
        imageMimeType = "image/jpeg",
    )

    @Test
    fun `execute returns a sessionId and Flow`() = runTest {
        evaluationPort.responseTokens = listOf("ACCEPT: Wniosek wstępnie pozytywny")

        val (sessionId, _) = useCase.execute(defaultCommand)
        assertNotNull(sessionId)
        assertTrue(sessionId.isNotBlank())
    }

    @Test
    fun `execute saves new session with null decision before streaming`() = runTest {
        evaluationPort.responseTokens = listOf("ACCEPT: Wniosek wstępnie pozytywny")

        val (sessionId, flow) = useCase.execute(defaultCommand)
        flow.toList()

        val savedSession = sessionRepository.savedSessions.first()
        assertEquals(sessionId, savedSession.id)
        assertEquals(RequestType.REKLAMACJA, savedSession.requestType)
        assertEquals(null, savedSession.decision)
    }

    @Test
    fun `execute returns Flow tokens from EvaluationPort`() = runTest {
        val tokens = listOf("ACCEPT:", " Wniosek", " wstępnie", " pozytywny")
        evaluationPort.responseTokens = tokens

        val (_, flow) = useCase.execute(defaultCommand)
        val emitted = flow.toList()

        assertEquals(tokens, emitted)
    }

    @Test
    fun `execute updates session decision to Accept when stream contains ACCEPT`() = runTest {
        evaluationPort.responseTokens = listOf("ACCEPT: Wniosek wstępnie pozytywny")

        val (sessionId, flow) = useCase.execute(defaultCommand)
        flow.toList()

        val updatedDecision = sessionRepository.updatedDecisions[sessionId]
        assertNotNull(updatedDecision)
        assertTrue(updatedDecision is Decision.Accept)
    }

    @Test
    fun `execute updates session decision to Reject when stream lacks ACCEPT`() = runTest {
        evaluationPort.responseTokens = listOf("REJECT: Wniosek wstępnie negatywny")

        val (sessionId, flow) = useCase.execute(defaultCommand)
        flow.toList()

        val updatedDecision = sessionRepository.updatedDecisions[sessionId]
        assertNotNull(updatedDecision)
        assertTrue(updatedDecision is Decision.Reject)
    }

    @Test
    fun `execute sets requestType from command`() = runTest {
        val zwrotCommand = defaultCommand.copy(requestType = RequestType.ZWROT)
        evaluationPort.responseTokens = listOf("ACCEPT: Wniosek wstępnie pozytywny")

        val (_, flow) = useCase.execute(zwrotCommand)
        flow.toList()

        val savedSession = sessionRepository.savedSessions.first()
        assertEquals(RequestType.ZWROT, savedSession.requestType)
    }
}

class FakeSessionRepository : SessionRepository {
    val savedSessions = mutableListOf<Session>()
    val updatedDecisions = mutableMapOf<String, Decision>()
    private val store = mutableMapOf<String, Session>()

    override suspend fun save(session: Session) {
        savedSessions.add(session)
        store[session.id] = session
    }

    override suspend fun findById(id: String): Session? = store[id]

    override suspend fun updateDecision(id: String, decision: Decision) {
        updatedDecisions[id] = decision
        store[id] = store[id]?.copy(decision = decision) ?: return
    }
}

class FakeEvaluationPort : EvaluationPort {
    var responseTokens: List<String> = emptyList()

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
    ): Flow<String> = flowOf(*responseTokens.toTypedArray())
}
