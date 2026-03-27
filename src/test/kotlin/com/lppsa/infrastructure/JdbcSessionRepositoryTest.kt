package com.lppsa.infrastructure

import com.lppsa.domain.model.Decision
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import com.lppsa.infrastructure.persistence.JdbcSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
class JdbcSessionRepositoryTest {

    @Autowired
    lateinit var jdbc: JdbcTemplate

    @Autowired
    lateinit var repository: JdbcSessionRepository

    @BeforeEach
    fun cleanUp() {
        jdbc.execute("DELETE FROM chat_messages")
        jdbc.execute("DELETE FROM sessions")
    }

    private fun session(id: String = "test-id") = Session(
        id = id,
        requestType = RequestType.REKLAMACJA,
        productName = "Bluzka",
        purchaseDate = "2025-01-15",
        description = "Szew się rozchodzi",
        createdAt = "2026-03-27T10:00:00",
    )

    @Test
    fun `save and findById returns session`() = runTest {
        val s = session()
        repository.save(s)
        val found = repository.findById(s.id)
        assertNotNull(found)
        assertEquals(s.id, found.id)
        assertEquals(s.requestType, found.requestType)
        assertEquals(s.productName, found.productName)
        assertEquals(s.purchaseDate, found.purchaseDate)
        assertEquals(s.description, found.description)
        assertNull(found.decision)
    }

    @Test
    fun `findById returns null for unknown id`() = runTest {
        assertNull(repository.findById("nonexistent"))
    }

    @Test
    fun `updateDecision sets Accept outcome`() = runTest {
        repository.save(session())
        val decision = Decision.Accept(explanation = "Wada potwierdzona", nextSteps = "Wypełnij formularz")
        repository.updateDecision("test-id", decision)
        val found = repository.findById("test-id")
        assertNotNull(found)
        val d = found.decision
        assertNotNull(d)
        assert(d is Decision.Accept)
        assertEquals("Wada potwierdzona", (d as Decision.Accept).explanation)
        assertEquals("Wypełnij formularz", d.nextSteps)
    }

    @Test
    fun `updateDecision sets Reject without mismatch`() = runTest {
        repository.save(session())
        val decision = Decision.Reject(explanation = "Brak wady")
        repository.updateDecision("test-id", decision)
        val found = repository.findById("test-id")
        val d = found!!.decision
        assert(d is Decision.Reject)
        assertNull((d as Decision.Reject).mismatchRecommendation)
    }

    @Test
    fun `updateDecision sets Reject with mismatch recommendation`() = runTest {
        repository.save(session())
        val decision = Decision.Reject(
            explanation = "To nie reklamacja",
            mismatchRecommendation = "Złóż wniosek jako Zwrot",
        )
        repository.updateDecision("test-id", decision)
        val found = repository.findById("test-id")
        val d = found!!.decision as Decision.Reject
        assertEquals("To nie reklamacja", d.explanation)
        assertEquals("Złóż wniosek jako Zwrot", d.mismatchRecommendation)
    }
}
