package com.lppsa.domain

import com.lppsa.domain.model.Decision
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class DecisionTest {
    @Test
    fun `Accept holds explanation and nextSteps`() {
        val d = Decision.Accept(explanation = "Produkt wadliwy", nextSteps = "Wypełnij formularz")
        assertEquals("Produkt wadliwy", d.explanation)
        assertEquals("Wypełnij formularz", d.nextSteps)
    }

    @Test
    fun `Reject holds explanation with null mismatch by default`() {
        val d = Decision.Reject(explanation = "Brak wady")
        assertEquals("Brak wady", d.explanation)
        assertNull(d.mismatchRecommendation)
    }

    @Test
    fun `Reject can hold mismatch recommendation`() {
        val d = Decision.Reject(explanation = "Brak wady", mismatchRecommendation = "Złóż zwrot")
        assertNotNull(d.mismatchRecommendation)
        assertEquals("Złóż zwrot", d.mismatchRecommendation)
    }

    @Test
    fun `Accept copy works`() {
        val d = Decision.Accept(explanation = "A", nextSteps = "B")
        val d2 = d.copy(explanation = "C")
        assertEquals("C", d2.explanation)
        assertEquals("B", d2.nextSteps)
    }
}
