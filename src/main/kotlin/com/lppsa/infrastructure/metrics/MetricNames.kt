package com.lppsa.infrastructure.metrics

/**
 * Single source of truth for all custom business metric names and their tag keys.
 *
 * Prometheus naming convention applied by Micrometer:
 *   - Dots (.) become underscores (_)
 *   - Counters get a `_total` suffix
 *   - Timers get `_seconds_bucket / _seconds_count / _seconds_sum` suffixes
 *
 * Example: [SESSION_SUBMITTED] = "sinsay.session.submitted"
 *   → Prometheus: sinsay_session_submitted_total{request_type="REKLAMACJA"}
 */
object MetricNames {

    // --- Counters ---

    /** sinsay_session_submitted_total{request_type} */
    const val SESSION_SUBMITTED = "sinsay.session.submitted"

    /** sinsay_session_decision_total{request_type, outcome} */
    const val SESSION_DECISION = "sinsay.session.decision"

    /** sinsay_chat_message_total{role} */
    const val CHAT_MESSAGE = "sinsay.chat.message"

    // --- Timers ---

    /** sinsay_ai_evaluation_duration_seconds_{bucket,count,sum}{request_type} */
    const val AI_EVALUATION_DURATION = "sinsay.ai.evaluation.duration"

    // --- Tag keys ---

    object Tags {
        const val REQUEST_TYPE = "request_type"
        const val OUTCOME = "outcome"
        const val ROLE = "role"
    }
}
