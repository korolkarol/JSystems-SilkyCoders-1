package com.lppsa.domain

import com.lppsa.domain.model.Decision
import com.lppsa.domain.model.RequestType
import com.lppsa.domain.model.Session
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class SessionTest {
    private fun session() = Session(
        id = "abc",
        requestType = RequestType.REKLAMACJA,
        productName = "Bluzka",
        purchaseDate = "2025-01-15",
        description = "Szwy się rozchodzą",
        createdAt = "2026-03-27T10:00:00",
    )

    @Test
    fun `session has null decision by default`() {
        assertNull(session().decision)
    }

    @Test
    fun `session holds all fields`() {
        val s = session()
        assertEquals("abc", s.id)
        assertEquals(RequestType.REKLAMACJA, s.requestType)
        assertEquals("Bluzka", s.productName)
        assertEquals("2025-01-15", s.purchaseDate)
        assertEquals("Szwy się rozchodzą", s.description)
    }

    @Test
    fun `copy updates decision`() {
        val decision = Decision.Accept(explanation = "OK", nextSteps = "Formularz")
        val updated = session().copy(decision = decision)
        assertNotNull(updated.decision)
        assertEquals(decision, updated.decision)
    }
}
