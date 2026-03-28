package com.lppsa.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class BusinessMetrics(private val registry: MeterRegistry) {

    fun recordSessionSubmitted(requestType: String) =
        registry.counter(
            MetricNames.SESSION_SUBMITTED,
            MetricNames.Tags.REQUEST_TYPE,
            requestType,
        ).increment()

    fun recordDecision(requestType: String, outcome: String) =
        registry.counter(
            MetricNames.SESSION_DECISION,
            MetricNames.Tags.REQUEST_TYPE,
            requestType,
            MetricNames.Tags.OUTCOME,
            outcome,
        ).increment()

    fun recordChatMessage(role: String) =
        registry.counter(
            MetricNames.CHAT_MESSAGE,
            MetricNames.Tags.ROLE,
            role,
        ).increment()

    fun evaluationTimer(requestType: String) =
        registry.timer(
            MetricNames.AI_EVALUATION_DURATION,
            MetricNames.Tags.REQUEST_TYPE,
            requestType,
        )
}
