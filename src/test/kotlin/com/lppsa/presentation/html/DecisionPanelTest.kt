package com.lppsa.presentation.html

import com.lppsa.domain.model.Decision
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DecisionPanelTest {

    private val sessionId = "test-session-42"

    @Test
    fun `renderDecisionPanel with Accept shows positive label`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Accept(
                explanation = "Produkt ma widoczną wadę.",
                nextSteps = "Wyślij produkt do sklepu.",
            ),
        )
        assertTrue(
            actual = html.contains("Wniosek wstępnie pozytywny"),
            message = "Accept panel must show positive label",
        )
    }

    @Test
    fun `renderDecisionPanel with Accept shows explanation`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Accept(
                explanation = "Produkt ma widoczną wadę.",
                nextSteps = "Wyślij produkt do sklepu.",
            ),
        )
        assertTrue(
            actual = html.contains("Produkt ma widoczną wadę."),
            message = "Accept panel must show explanation",
        )
    }

    @Test
    fun `renderDecisionPanel with Accept shows next steps`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Accept(
                explanation = "Produkt ma widoczną wadę.",
                nextSteps = "Wyślij produkt do sklepu.",
            ),
        )
        assertTrue(
            actual = html.contains("Wyślij produkt do sklepu."),
            message = "Accept panel must show next steps",
        )
    }

    @Test
    fun `renderDecisionPanel with Accept includes chat section`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Accept(
                explanation = "Produkt ma widoczną wadę.",
                nextSteps = "Wyślij produkt do sklepu.",
            ),
        )
        assertTrue(
            actual = html.contains("id=\"chat-messages\""),
            message = "Accept panel must include chat-messages div",
        )
        assertTrue(
            actual = html.contains("hx-post=\"/chat/$sessionId\""),
            message = "Accept panel must include chat form with correct sessionId",
        )
    }

    @Test
    fun `renderDecisionPanel with Reject shows negative label`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Reject(explanation = "Produkt wygląda na normalnie używany."),
        )
        assertTrue(
            actual = html.contains("Wniosek wstępnie negatywny"),
            message = "Reject panel must show negative label",
        )
    }

    @Test
    fun `renderDecisionPanel with Reject shows reason`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Reject(explanation = "Produkt wygląda na normalnie używany."),
        )
        assertTrue(
            actual = html.contains("Produkt wygląda na normalnie używany."),
            message = "Reject panel must show reason",
        )
    }

    @Test
    fun `renderDecisionPanel with Reject without mismatch omits mismatch section`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Reject(
                explanation = "reason",
                mismatchRecommendation = null,
            ),
        )
        assertTrue(
            actual = !html.contains("mismatch rec"),
            message = "No mismatch recommendation should be shown when null",
        )
    }

    @Test
    fun `renderDecisionPanel with Reject with mismatch shows recommendation`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Reject(
                explanation = "reason",
                mismatchRecommendation = "mismatch rec",
            ),
        )
        assertTrue(
            actual = html.contains("mismatch rec"),
            message = "Mismatch recommendation must be shown",
        )
    }

    @Test
    fun `renderDecisionPanel chat form has correct target and swap`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Accept(
                explanation = "ok",
                nextSteps = "steps",
            ),
        )
        assertTrue(
            actual = html.contains("hx-target=\"#chat-messages\""),
            message = "Chat form must target chat-messages",
        )
        assertTrue(
            actual = html.contains("hx-swap=\"beforeend\""),
            message = "Chat form must use beforeend swap",
        )
    }

    @Test
    fun `renderDecisionPanel chat send button has Polish label`() {
        val html = renderDecisionPanel(
            sessionId = sessionId,
            decision = Decision.Accept(
                explanation = "ok",
                nextSteps = "steps",
            ),
        )
        assertTrue(
            actual = html.contains("WYŚLIJ"),
            message = "Chat send button must say WYŚLIJ",
        )
    }
}
