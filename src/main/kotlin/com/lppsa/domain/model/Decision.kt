package com.lppsa.domain.model

sealed class Decision {
    data class Accept(
        val explanation: String,
        val nextSteps: String,
    ) : Decision()

    data class Reject(
        val explanation: String,
        val mismatchRecommendation: String? = null,
    ) : Decision()
}
