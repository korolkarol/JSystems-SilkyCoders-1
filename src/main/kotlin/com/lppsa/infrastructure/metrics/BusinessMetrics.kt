package com.lppsa.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class BusinessMetrics(private val registry: MeterRegistry) {

    fun recordSessionSubmitted(requestType: String) =
        registry.counter("sinsay.session.submitted", "request_type", requestType).increment()

    fun recordDecision(requestType: String, outcome: String) =
        registry.counter("sinsay.session.decision", "request_type", requestType, "outcome", outcome).increment()

    fun recordChatMessage(role: String) =
        registry.counter("sinsay.chat.message", "role", role).increment()

    fun evaluationTimer(requestType: String) =
        registry.timer("sinsay.ai.evaluation.duration", "request_type", requestType)
}
